package com.shopapp.vendor.service;

import com.shopapp.shared.events.DomainEventPublisher;
import com.shopapp.shared.events.vendor.VendorApprovedEvent;
import com.shopapp.shared.events.vendor.VendorRejectedEvent;
import com.shopapp.shared.exception.BadRequestException;
import com.shopapp.shared.exception.ConflictException;
import com.shopapp.shared.exception.ForbiddenException;
import com.shopapp.shared.exception.ResourceNotFoundException;
import com.shopapp.shared.interfaces.VendorModuleApi;
import com.shopapp.vendor.domain.Vendor;
import com.shopapp.vendor.domain.VendorStatus;
import com.shopapp.vendor.dto.UpdateVendorRequest;
import com.shopapp.vendor.dto.VendorRegistrationRequest;
import com.shopapp.vendor.dto.VendorResponse;
import com.shopapp.vendor.repository.VendorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class VendorService implements VendorModuleApi {

    private final VendorRepository vendorRepository;
    private final DomainEventPublisher eventPublisher;

    // ===== VendorModuleApi Implementation =====

    @Override
    public Optional<VendorDto> findByUserId(String userId) {
        return vendorRepository.findByUserId(userId)
                .map(this::toVendorDto);
    }

    @Override
    public Optional<VendorDto> findById(String vendorId) {
        return vendorRepository.findById(vendorId)
                .map(this::toVendorDto);
    }

    @Override
    public boolean isApprovedVendor(String userId) {
        return vendorRepository.findByUserId(userId)
                .map(Vendor::isApproved)
                .orElse(false);
    }

    @Override
    public Optional<String> getVendorIdByUserId(String userId) {
        return vendorRepository.findByUserId(userId)
                .filter(Vendor::isApproved)
                .map(Vendor::getId);
    }

    // ===== Vendor Registration & Profile =====

    @Transactional
    public VendorResponse registerAsVendor(String userId, VendorRegistrationRequest request) {
        log.info("Registering user {} as vendor", userId);

        // Check if user already has a vendor profile
        if (vendorRepository.existsByUserId(userId)) {
            throw new ConflictException("User already has a vendor profile");
        }

        Vendor vendor = Vendor.builder()
                .userId(userId)
                .businessName(request.getBusinessName())
                .description(request.getDescription())
                .contactEmail(request.getContactEmail())
                .contactPhone(request.getContactPhone())
                .status(VendorStatus.PENDING)
                .build();

        Vendor savedVendor = vendorRepository.save(vendor);
        log.info("Vendor registration submitted with id: {}", savedVendor.getId());

        return toVendorResponse(savedVendor);
    }

    public VendorResponse getVendorProfile(String userId) {
        Vendor vendor = vendorRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor", "userId", userId));
        return toVendorResponse(vendor);
    }

    public VendorResponse getVendorById(String vendorId) {
        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor", "id", vendorId));
        return toVendorResponse(vendor);
    }

    @Transactional
    public VendorResponse updateVendorProfile(String userId, UpdateVendorRequest request) {
        Vendor vendor = vendorRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor", "userId", userId));

        if (request.getBusinessName() != null) {
            vendor.setBusinessName(request.getBusinessName());
        }
        if (request.getDescription() != null) {
            vendor.setDescription(request.getDescription());
        }
        if (request.getContactEmail() != null) {
            vendor.setContactEmail(request.getContactEmail());
        }
        if (request.getContactPhone() != null) {
            vendor.setContactPhone(request.getContactPhone());
        }

        Vendor updatedVendor = vendorRepository.save(vendor);
        log.info("Updated vendor profile for user: {}", userId);

        return toVendorResponse(updatedVendor);
    }

    // ===== Admin Operations =====

    public Page<VendorResponse> getAllVendors(Pageable pageable) {
        return vendorRepository.findAll(pageable)
                .map(this::toVendorResponse);
    }

    public Page<VendorResponse> getPendingVendors(Pageable pageable) {
        return vendorRepository.findByStatus(VendorStatus.PENDING, pageable)
                .map(this::toVendorResponse);
    }

    @Transactional
    public VendorResponse approveVendor(String vendorId) {
        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor", "id", vendorId));

        if (!vendor.isPending()) {
            throw new BadRequestException("Only pending vendors can be approved");
        }

        vendor.setStatus(VendorStatus.APPROVED);
        vendor.setApprovedAt(LocalDateTime.now());
        vendor.setRejectionReason(null);

        Vendor approvedVendor = vendorRepository.save(vendor);
        log.info("Vendor {} approved", vendorId);

        // Publish event to add VENDOR role to user
        eventPublisher.publish(new VendorApprovedEvent(vendorId, vendor.getUserId()));

        return toVendorResponse(approvedVendor);
    }

    @Transactional
    public VendorResponse rejectVendor(String vendorId, String reason) {
        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor", "id", vendorId));

        if (!vendor.isPending()) {
            throw new BadRequestException("Only pending vendors can be rejected");
        }

        vendor.setStatus(VendorStatus.REJECTED);
        vendor.setRejectionReason(reason);

        Vendor rejectedVendor = vendorRepository.save(vendor);
        log.info("Vendor {} rejected with reason: {}", vendorId, reason);

        // Publish event
        eventPublisher.publish(new VendorRejectedEvent(vendorId, vendor.getUserId(), reason));

        return toVendorResponse(rejectedVendor);
    }

    // ===== Helper Methods =====

    private VendorDto toVendorDto(Vendor vendor) {
        return new VendorDto(
                vendor.getId(),
                vendor.getUserId(),
                vendor.getBusinessName(),
                vendor.getDescription(),
                vendor.getContactEmail(),
                vendor.getStatus().name()
        );
    }

    private VendorResponse toVendorResponse(Vendor vendor) {
        return VendorResponse.builder()
                .id(vendor.getId())
                .userId(vendor.getUserId())
                .businessName(vendor.getBusinessName())
                .description(vendor.getDescription())
                .contactEmail(vendor.getContactEmail())
                .contactPhone(vendor.getContactPhone())
                .status(vendor.getStatus())
                .rejectionReason(vendor.getRejectionReason())
                .createdAt(vendor.getCreatedAt())
                .approvedAt(vendor.getApprovedAt())
                .build();
    }
}
