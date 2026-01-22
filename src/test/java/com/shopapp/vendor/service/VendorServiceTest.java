package com.shopapp.vendor.service;

import com.shopapp.shared.events.DomainEventPublisher;
import com.shopapp.shared.events.vendor.VendorApprovedEvent;
import com.shopapp.shared.events.vendor.VendorRejectedEvent;
import com.shopapp.shared.exception.BadRequestException;
import com.shopapp.shared.exception.ConflictException;
import com.shopapp.shared.exception.ResourceNotFoundException;
import com.shopapp.shared.interfaces.VendorModuleApi;
import com.shopapp.vendor.domain.Vendor;
import com.shopapp.vendor.domain.VendorStatus;
import com.shopapp.vendor.dto.UpdateVendorRequest;
import com.shopapp.vendor.dto.VendorRegistrationRequest;
import com.shopapp.vendor.dto.VendorResponse;
import com.shopapp.vendor.repository.VendorRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("VendorService Tests")
class VendorServiceTest {

    @Mock
    private VendorRepository vendorRepository;

    @Mock
    private DomainEventPublisher eventPublisher;

    @InjectMocks
    private VendorService vendorService;

    @Nested
    @DisplayName("Vendor Registration")
    class VendorRegistration {

        @Test
        @DisplayName("Should register new vendor with PENDING status")
        void shouldRegisterNewVendorWithPendingStatus() {
            VendorRegistrationRequest request = VendorRegistrationRequest.builder()
                    .businessName("Tech Store")
                    .description("Best electronics store")
                    .contactEmail("vendor@techstore.com")
                    .contactPhone("+1234567890")
                    .build();

            Vendor savedVendor = Vendor.builder()
                    .id("vendorId123")
                    .userId("userId123")
                    .businessName("Tech Store")
                    .description("Best electronics store")
                    .contactEmail("vendor@techstore.com")
                    .contactPhone("+1234567890")
                    .status(VendorStatus.PENDING)
                    .build();

            when(vendorRepository.existsByUserId("userId123")).thenReturn(false);
            when(vendorRepository.save(any(Vendor.class))).thenReturn(savedVendor);

            VendorResponse response = vendorService.registerAsVendor("userId123", request);

            assertNotNull(response);
            assertEquals("vendorId123", response.getId());
            assertEquals("Tech Store", response.getBusinessName());
            assertEquals(VendorStatus.PENDING, response.getStatus());
        }

        @Test
        @DisplayName("Should throw ConflictException when user already has vendor profile")
        void shouldThrowConflictExceptionWhenAlreadyRegistered() {
            VendorRegistrationRequest request = VendorRegistrationRequest.builder()
                    .businessName("Tech Store")
                    .build();

            when(vendorRepository.existsByUserId("userId123")).thenReturn(true);

            ConflictException exception = assertThrows(ConflictException.class, 
                    () -> vendorService.registerAsVendor("userId123", request));
            
            assertEquals("User already has a vendor profile", exception.getMessage());
            verify(vendorRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should save all vendor details correctly")
        void shouldSaveAllVendorDetailsCorrectly() {
            VendorRegistrationRequest request = VendorRegistrationRequest.builder()
                    .businessName("Tech Store")
                    .description("Best electronics")
                    .contactEmail("vendor@store.com")
                    .contactPhone("+1234567890")
                    .build();

            when(vendorRepository.existsByUserId(anyString())).thenReturn(false);
            when(vendorRepository.save(any(Vendor.class))).thenAnswer(i -> i.getArgument(0));

            vendorService.registerAsVendor("userId123", request);

            ArgumentCaptor<Vendor> vendorCaptor = ArgumentCaptor.forClass(Vendor.class);
            verify(vendorRepository).save(vendorCaptor.capture());

            Vendor savedVendor = vendorCaptor.getValue();
            assertEquals("userId123", savedVendor.getUserId());
            assertEquals("Tech Store", savedVendor.getBusinessName());
            assertEquals("Best electronics", savedVendor.getDescription());
            assertEquals("vendor@store.com", savedVendor.getContactEmail());
            assertEquals(VendorStatus.PENDING, savedVendor.getStatus());
        }
    }

    @Nested
    @DisplayName("VendorModuleApi Implementation")
    class VendorModuleApiImplementation {

        @Test
        @DisplayName("Should find vendor by user ID")
        void shouldFindVendorByUserId() {
            Vendor vendor = Vendor.builder()
                    .id("vendorId123")
                    .userId("userId123")
                    .businessName("Tech Store")
                    .status(VendorStatus.APPROVED)
                    .build();

            when(vendorRepository.findByUserId("userId123")).thenReturn(Optional.of(vendor));

            Optional<VendorModuleApi.VendorDto> result = vendorService.findByUserId("userId123");

            assertTrue(result.isPresent());
            assertEquals("vendorId123", result.get().id());
            assertEquals("Tech Store", result.get().businessName());
        }

        @Test
        @DisplayName("Should return empty when vendor not found by user ID")
        void shouldReturnEmptyWhenVendorNotFoundByUserId() {
            when(vendorRepository.findByUserId("nonexistent")).thenReturn(Optional.empty());

            Optional<VendorModuleApi.VendorDto> result = vendorService.findByUserId("nonexistent");

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Should find vendor by vendor ID")
        void shouldFindVendorByVendorId() {
            Vendor vendor = Vendor.builder()
                    .id("vendorId123")
                    .userId("userId123")
                    .businessName("Tech Store")
                    .status(VendorStatus.APPROVED)
                    .build();

            when(vendorRepository.findById("vendorId123")).thenReturn(Optional.of(vendor));

            Optional<VendorModuleApi.VendorDto> result = vendorService.findById("vendorId123");

            assertTrue(result.isPresent());
            assertEquals("userId123", result.get().userId());
        }

        @Test
        @DisplayName("Should return true for approved vendor")
        void shouldReturnTrueForApprovedVendor() {
            Vendor vendor = Vendor.builder()
                    .id("vendorId123")
                    .userId("userId123")
                    .status(VendorStatus.APPROVED)
                    .build();

            when(vendorRepository.findByUserId("userId123")).thenReturn(Optional.of(vendor));

            assertTrue(vendorService.isApprovedVendor("userId123"));
        }

        @Test
        @DisplayName("Should return false for pending vendor")
        void shouldReturnFalseForPendingVendor() {
            Vendor vendor = Vendor.builder()
                    .id("vendorId123")
                    .userId("userId123")
                    .status(VendorStatus.PENDING)
                    .build();

            when(vendorRepository.findByUserId("userId123")).thenReturn(Optional.of(vendor));

            assertFalse(vendorService.isApprovedVendor("userId123"));
        }

        @Test
        @DisplayName("Should return false for rejected vendor")
        void shouldReturnFalseForRejectedVendor() {
            Vendor vendor = Vendor.builder()
                    .id("vendorId123")
                    .userId("userId123")
                    .status(VendorStatus.REJECTED)
                    .build();

            when(vendorRepository.findByUserId("userId123")).thenReturn(Optional.of(vendor));

            assertFalse(vendorService.isApprovedVendor("userId123"));
        }

        @Test
        @DisplayName("Should return false when vendor not found")
        void shouldReturnFalseWhenVendorNotFound() {
            when(vendorRepository.findByUserId("nonexistent")).thenReturn(Optional.empty());

            assertFalse(vendorService.isApprovedVendor("nonexistent"));
        }

        @Test
        @DisplayName("Should return vendor ID for approved vendor")
        void shouldReturnVendorIdForApprovedVendor() {
            Vendor vendor = Vendor.builder()
                    .id("vendorId123")
                    .userId("userId123")
                    .status(VendorStatus.APPROVED)
                    .build();

            when(vendorRepository.findByUserId("userId123")).thenReturn(Optional.of(vendor));

            Optional<String> result = vendorService.getVendorIdByUserId("userId123");

            assertTrue(result.isPresent());
            assertEquals("vendorId123", result.get());
        }

        @Test
        @DisplayName("Should return empty for pending vendor when getting vendor ID")
        void shouldReturnEmptyForPendingVendor() {
            Vendor vendor = Vendor.builder()
                    .id("vendorId123")
                    .userId("userId123")
                    .status(VendorStatus.PENDING)
                    .build();

            when(vendorRepository.findByUserId("userId123")).thenReturn(Optional.of(vendor));

            Optional<String> result = vendorService.getVendorIdByUserId("userId123");

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("Vendor Profile Management")
    class VendorProfileManagement {

        @Test
        @DisplayName("Should get vendor profile by user ID")
        void shouldGetVendorProfileByUserId() {
            Vendor vendor = Vendor.builder()
                    .id("vendorId123")
                    .userId("userId123")
                    .businessName("Tech Store")
                    .description("Best electronics")
                    .contactEmail("vendor@store.com")
                    .status(VendorStatus.APPROVED)
                    .build();

            when(vendorRepository.findByUserId("userId123")).thenReturn(Optional.of(vendor));

            VendorResponse response = vendorService.getVendorProfile("userId123");

            assertEquals("vendorId123", response.getId());
            assertEquals("Tech Store", response.getBusinessName());
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when getting non-existent profile")
        void shouldThrowExceptionForNonExistentProfile() {
            when(vendorRepository.findByUserId("nonexistent")).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, 
                    () -> vendorService.getVendorProfile("nonexistent"));
        }

        @Test
        @DisplayName("Should update vendor business name")
        void shouldUpdateVendorBusinessName() {
            Vendor vendor = Vendor.builder()
                    .id("vendorId123")
                    .userId("userId123")
                    .businessName("Old Name")
                    .status(VendorStatus.APPROVED)
                    .build();

            UpdateVendorRequest request = UpdateVendorRequest.builder()
                    .businessName("New Name")
                    .build();

            when(vendorRepository.findByUserId("userId123")).thenReturn(Optional.of(vendor));
            when(vendorRepository.save(any(Vendor.class))).thenReturn(vendor);

            VendorResponse response = vendorService.updateVendorProfile("userId123", request);

            assertEquals("New Name", response.getBusinessName());
        }

        @Test
        @DisplayName("Should update vendor description")
        void shouldUpdateVendorDescription() {
            Vendor vendor = Vendor.builder()
                    .id("vendorId123")
                    .userId("userId123")
                    .description("Old description")
                    .status(VendorStatus.APPROVED)
                    .build();

            UpdateVendorRequest request = UpdateVendorRequest.builder()
                    .description("New description")
                    .build();

            when(vendorRepository.findByUserId("userId123")).thenReturn(Optional.of(vendor));
            when(vendorRepository.save(any(Vendor.class))).thenReturn(vendor);

            VendorResponse response = vendorService.updateVendorProfile("userId123", request);

            assertEquals("New description", response.getDescription());
        }

        @Test
        @DisplayName("Should update multiple vendor fields")
        void shouldUpdateMultipleVendorFields() {
            Vendor vendor = Vendor.builder()
                    .id("vendorId123")
                    .userId("userId123")
                    .businessName("Old Name")
                    .contactEmail("old@email.com")
                    .contactPhone("111")
                    .status(VendorStatus.APPROVED)
                    .build();

            UpdateVendorRequest request = UpdateVendorRequest.builder()
                    .businessName("New Name")
                    .contactEmail("new@email.com")
                    .contactPhone("999")
                    .build();

            when(vendorRepository.findByUserId("userId123")).thenReturn(Optional.of(vendor));
            when(vendorRepository.save(any(Vendor.class))).thenReturn(vendor);

            VendorResponse response = vendorService.updateVendorProfile("userId123", request);

            assertEquals("New Name", response.getBusinessName());
            assertEquals("new@email.com", response.getContactEmail());
            assertEquals("999", response.getContactPhone());
        }
    }

    @Nested
    @DisplayName("Vendor Approval Workflow")
    class VendorApprovalWorkflow {

        @Test
        @DisplayName("Should approve pending vendor")
        void shouldApprovePendingVendor() {
            Vendor vendor = Vendor.builder()
                    .id("vendorId123")
                    .userId("userId123")
                    .businessName("Tech Store")
                    .status(VendorStatus.PENDING)
                    .build();

            when(vendorRepository.findById("vendorId123")).thenReturn(Optional.of(vendor));
            when(vendorRepository.save(any(Vendor.class))).thenReturn(vendor);

            VendorResponse response = vendorService.approveVendor("vendorId123");

            assertEquals(VendorStatus.APPROVED, response.getStatus());
            assertNotNull(vendor.getApprovedAt());
            assertNull(response.getRejectionReason());
        }

        @Test
        @DisplayName("Should publish VendorApprovedEvent on approval")
        void shouldPublishVendorApprovedEventOnApproval() {
            Vendor vendor = Vendor.builder()
                    .id("vendorId123")
                    .userId("userId123")
                    .status(VendorStatus.PENDING)
                    .build();

            when(vendorRepository.findById("vendorId123")).thenReturn(Optional.of(vendor));
            when(vendorRepository.save(any(Vendor.class))).thenReturn(vendor);

            vendorService.approveVendor("vendorId123");

            ArgumentCaptor<VendorApprovedEvent> eventCaptor = ArgumentCaptor.forClass(VendorApprovedEvent.class);
            verify(eventPublisher).publish(eventCaptor.capture());

            assertEquals("vendorId123", eventCaptor.getValue().getVendorId());
            assertEquals("userId123", eventCaptor.getValue().getUserId());
        }

        @Test
        @DisplayName("Should throw BadRequestException when approving non-pending vendor")
        void shouldThrowExceptionWhenApprovingNonPendingVendor() {
            Vendor vendor = Vendor.builder()
                    .id("vendorId123")
                    .status(VendorStatus.APPROVED)
                    .build();

            when(vendorRepository.findById("vendorId123")).thenReturn(Optional.of(vendor));

            BadRequestException exception = assertThrows(BadRequestException.class, 
                    () -> vendorService.approveVendor("vendorId123"));
            
            assertEquals("Only pending vendors can be approved", exception.getMessage());
            verify(eventPublisher, never()).publish(any());
        }

        @Test
        @DisplayName("Should reject pending vendor with reason")
        void shouldRejectPendingVendorWithReason() {
            Vendor vendor = Vendor.builder()
                    .id("vendorId123")
                    .userId("userId123")
                    .status(VendorStatus.PENDING)
                    .build();

            when(vendorRepository.findById("vendorId123")).thenReturn(Optional.of(vendor));
            when(vendorRepository.save(any(Vendor.class))).thenReturn(vendor);

            VendorResponse response = vendorService.rejectVendor("vendorId123", "Invalid documents");

            assertEquals(VendorStatus.REJECTED, response.getStatus());
            assertEquals("Invalid documents", response.getRejectionReason());
        }

        @Test
        @DisplayName("Should publish VendorRejectedEvent on rejection")
        void shouldPublishVendorRejectedEventOnRejection() {
            Vendor vendor = Vendor.builder()
                    .id("vendorId123")
                    .userId("userId123")
                    .status(VendorStatus.PENDING)
                    .build();

            when(vendorRepository.findById("vendorId123")).thenReturn(Optional.of(vendor));
            when(vendorRepository.save(any(Vendor.class))).thenReturn(vendor);

            vendorService.rejectVendor("vendorId123", "Invalid documents");

            ArgumentCaptor<VendorRejectedEvent> eventCaptor = ArgumentCaptor.forClass(VendorRejectedEvent.class);
            verify(eventPublisher).publish(eventCaptor.capture());

            assertEquals("vendorId123", eventCaptor.getValue().getVendorId());
            assertEquals("userId123", eventCaptor.getValue().getUserId());
            assertEquals("Invalid documents", eventCaptor.getValue().getReason());
        }

        @Test
        @DisplayName("Should throw BadRequestException when rejecting non-pending vendor")
        void shouldThrowExceptionWhenRejectingNonPendingVendor() {
            Vendor vendor = Vendor.builder()
                    .id("vendorId123")
                    .status(VendorStatus.REJECTED)
                    .build();

            when(vendorRepository.findById("vendorId123")).thenReturn(Optional.of(vendor));

            BadRequestException exception = assertThrows(BadRequestException.class, 
                    () -> vendorService.rejectVendor("vendorId123", "Some reason"));
            
            assertEquals("Only pending vendors can be rejected", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Admin Operations")
    class AdminOperations {

        @Test
        @DisplayName("Should get all vendors with pagination")
        void shouldGetAllVendorsWithPagination() {
            Vendor vendor1 = Vendor.builder().id("1").businessName("Store 1")
                    .status(VendorStatus.APPROVED).build();
            Vendor vendor2 = Vendor.builder().id("2").businessName("Store 2")
                    .status(VendorStatus.PENDING).build();

            Pageable pageable = PageRequest.of(0, 10);
            Page<Vendor> vendorPage = new PageImpl<>(List.of(vendor1, vendor2), pageable, 2);

            when(vendorRepository.findAll(pageable)).thenReturn(vendorPage);

            Page<VendorResponse> result = vendorService.getAllVendors(pageable);

            assertEquals(2, result.getContent().size());
        }

        @Test
        @DisplayName("Should get only pending vendors")
        void shouldGetOnlyPendingVendors() {
            Vendor pendingVendor = Vendor.builder().id("1").businessName("Store 1")
                    .status(VendorStatus.PENDING).build();

            Pageable pageable = PageRequest.of(0, 10);
            Page<Vendor> vendorPage = new PageImpl<>(List.of(pendingVendor), pageable, 1);

            when(vendorRepository.findByStatus(VendorStatus.PENDING, pageable)).thenReturn(vendorPage);

            Page<VendorResponse> result = vendorService.getPendingVendors(pageable);

            assertEquals(1, result.getContent().size());
            assertEquals(VendorStatus.PENDING, result.getContent().get(0).getStatus());
        }
    }
}
