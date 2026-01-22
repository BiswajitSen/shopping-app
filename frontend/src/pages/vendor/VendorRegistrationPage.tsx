import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Store, AlertCircle, CheckCircle } from 'lucide-react';
import { useMutation } from '@tanstack/react-query';
import { vendorsApi } from '../../api/vendors';
import { useAuthStore } from '../../store/authStore';
import { LoadingSpinner } from '../../components/common/LoadingSpinner';
import { VendorRegistrationRequest, UserRole } from '../../types';

export function VendorRegistrationPage() {
  const navigate = useNavigate();
  const { isAuthenticated, user } = useAuthStore();
  
  const [formData, setFormData] = useState<VendorRegistrationRequest>({
    businessName: '',
    description: '',
    contactEmail: '',
    contactPhone: '',
  });
  const [error, setError] = useState('');
  const [success, setSuccess] = useState(false);

  // Redirect to vendor dashboard if user is already a vendor
  useEffect(() => {
    if (isAuthenticated && user?.roles?.includes(UserRole.VENDOR)) {
      navigate('/vendor/dashboard', { replace: true });
    }
  }, [isAuthenticated, user, navigate]);

  const mutation = useMutation({
    mutationFn: vendorsApi.register,
    onSuccess: () => {
      setSuccess(true);
    },
    onError: (err: unknown) => {
      if (err && typeof err === 'object' && 'response' in err) {
        const axiosError = err as { response?: { data?: { message?: string } } };
        setError(axiosError.response?.data?.message || 'Registration failed. Please try again.');
      } else {
        setError('Registration failed. Please try again.');
      }
    },
  });

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    mutation.mutate(formData);
  };

  // Show loading while checking/redirecting
  if (isAuthenticated && user?.roles?.includes(UserRole.VENDOR)) {
    return (
      <div className="flex items-center justify-center min-h-[400px]">
        <LoadingSpinner size="lg" />
      </div>
    );
  }

  if (!isAuthenticated) {
    return (
      <div className="max-w-2xl mx-auto px-4 py-12 text-center">
        <Store className="h-16 w-16 text-gray-300 mx-auto mb-4" />
        <h2 className="text-2xl font-bold text-gray-900 mb-2">Sign in Required</h2>
        <p className="text-gray-600 mb-4">
          You need to be logged in to register as a vendor.
        </p>
        <button
          onClick={() => navigate('/login', { state: { from: '/vendor/register' } })}
          className="btn btn-primary"
        >
          Sign In
        </button>
      </div>
    );
  }

  if (success) {
    return (
      <div className="max-w-2xl mx-auto px-4 py-12 text-center">
        <CheckCircle className="h-16 w-16 text-green-500 mx-auto mb-4" />
        <h2 className="text-2xl font-bold text-gray-900 mb-2">Application Submitted!</h2>
        <p className="text-gray-600 mb-4">
          Your vendor application has been submitted successfully. Our team will review 
          your application and get back to you within 2-3 business days.
        </p>
        <button
          onClick={() => navigate('/')}
          className="btn btn-primary"
        >
          Return to Home
        </button>
      </div>
    );
  }

  return (
    <div className="max-w-2xl mx-auto px-4 py-12">
      <div className="text-center mb-8">
        <Store className="h-12 w-12 text-primary-600 mx-auto mb-4" />
        <h1 className="text-3xl font-bold text-gray-900">Become a Vendor</h1>
        <p className="text-gray-600 mt-2">
          Start selling your products on our marketplace
        </p>
      </div>

      <form onSubmit={handleSubmit} className="card p-8 space-y-6">
        {error && (
          <div className="bg-red-50 border border-red-200 rounded-lg p-4 flex items-center space-x-2 text-red-700">
            <AlertCircle className="h-5 w-5 flex-shrink-0" />
            <span className="text-sm">{error}</span>
          </div>
        )}

        {/* Business Info */}
        <div>
          <h3 className="text-lg font-semibold text-gray-900 mb-4">Business Information</h3>
          <div className="space-y-4">
            <div>
              <label htmlFor="businessName" className="label">
                Business Name *
              </label>
              <input
                id="businessName"
                name="businessName"
                type="text"
                required
                value={formData.businessName}
                onChange={handleChange}
                className="input"
                placeholder="Your Business Name"
              />
            </div>

            <div>
              <label htmlFor="description" className="label">
                Business Description *
              </label>
              <textarea
                id="description"
                name="description"
                required
                rows={4}
                value={formData.description}
                onChange={handleChange}
                className="input resize-none"
                placeholder="Tell us about your business and products..."
              />
            </div>
          </div>
        </div>

        {/* Contact Info */}
        <div>
          <h3 className="text-lg font-semibold text-gray-900 mb-4">Contact Information</h3>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label htmlFor="contactEmail" className="label">
                Contact Email *
              </label>
              <input
                id="contactEmail"
                name="contactEmail"
                type="email"
                required
                value={formData.contactEmail}
                onChange={handleChange}
                className="input"
                placeholder="business@example.com"
              />
            </div>

            <div>
              <label htmlFor="contactPhone" className="label">
                Contact Phone
              </label>
              <input
                id="contactPhone"
                name="contactPhone"
                type="tel"
                value={formData.contactPhone}
                onChange={handleChange}
                className="input"
                placeholder="+1 (555) 000-0000"
              />
            </div>
          </div>
        </div>

        <button
          type="submit"
          disabled={mutation.isPending}
          className="btn btn-primary w-full py-3 text-lg"
        >
          {mutation.isPending ? (
            <LoadingSpinner size="sm" className="text-white" />
          ) : (
            'Submit Application'
          )}
        </button>

        <p className="text-sm text-gray-500 text-center">
          By submitting this application, you agree to our{' '}
          <a href="#" className="text-primary-600 hover:text-primary-500">
            Vendor Terms & Conditions
          </a>
        </p>
      </form>
    </div>
  );
}
