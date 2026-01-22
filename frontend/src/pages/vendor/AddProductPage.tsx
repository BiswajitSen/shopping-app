import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { ArrowLeft, AlertCircle, Plus, X } from 'lucide-react';
import { useMutation } from '@tanstack/react-query';
import { productsApi } from '../../api/products';
import { CreateProductRequest } from '../../types';
import { LoadingSpinner } from '../../components/common/LoadingSpinner';
import toast from 'react-hot-toast';

const categories = [
  'Electronics',
  'Clothing',
  'Home & Garden',
  'Sports',
  'Books',
  'Toys',
  'Beauty',
  'Automotive',
  'Food & Grocery',
  'Other',
];

export function AddProductPage() {
  const navigate = useNavigate();
  const [formData, setFormData] = useState<CreateProductRequest>({
    name: '',
    description: '',
    category: '',
    price: 0,
    stock: 0,
    images: [],
  });
  const [imageUrl, setImageUrl] = useState('');
  const [error, setError] = useState('');

  const mutation = useMutation({
    mutationFn: productsApi.create,
    onSuccess: () => {
      toast.success('Product created successfully! It will be reviewed by admin.');
      navigate('/vendor/dashboard');
    },
    onError: (err: unknown) => {
      if (err && typeof err === 'object' && 'response' in err) {
        const axiosError = err as { response?: { data?: { message?: string } } };
        setError(axiosError.response?.data?.message || 'Failed to create product');
      } else {
        setError('Failed to create product');
      }
    },
  });

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: name === 'price' || name === 'stock' ? parseFloat(value) || 0 : value,
    }));
  };

  const addImage = () => {
    if (imageUrl.trim()) {
      setFormData((prev) => ({
        ...prev,
        images: [...(prev.images || []), imageUrl.trim()],
      }));
      setImageUrl('');
    }
  };

  const removeImage = (index: number) => {
    setFormData((prev) => ({
      ...prev,
      images: prev.images?.filter((_, i) => i !== index) || [],
    }));
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    setError('');

    if (!formData.name || !formData.category || formData.price <= 0 || formData.stock < 0) {
      setError('Please fill in all required fields');
      return;
    }

    mutation.mutate(formData);
  };

  return (
    <div className="max-w-3xl mx-auto px-4 py-8">
      {/* Header */}
      <div className="mb-8">
        <button
          onClick={() => navigate('/vendor/dashboard')}
          className="flex items-center text-gray-600 hover:text-gray-900 mb-4"
        >
          <ArrowLeft className="h-4 w-4 mr-2" />
          Back to Dashboard
        </button>
        <h1 className="text-3xl font-bold text-gray-900">Add New Product</h1>
        <p className="text-gray-600 mt-1">
          Fill in the details below to list a new product
        </p>
      </div>

      <form onSubmit={handleSubmit} className="card p-8 space-y-6">
        {error && (
          <div className="bg-red-50 border border-red-200 rounded-lg p-4 flex items-center space-x-2 text-red-700">
            <AlertCircle className="h-5 w-5 flex-shrink-0" />
            <span className="text-sm">{error}</span>
          </div>
        )}

        {/* Basic Info */}
        <div>
          <h3 className="text-lg font-semibold text-gray-900 mb-4">Basic Information</h3>
          <div className="space-y-4">
            <div>
              <label htmlFor="name" className="label">
                Product Name *
              </label>
              <input
                id="name"
                name="name"
                type="text"
                required
                value={formData.name}
                onChange={handleChange}
                className="input"
                placeholder="Enter product name"
              />
            </div>

            <div>
              <label htmlFor="description" className="label">
                Description *
              </label>
              <textarea
                id="description"
                name="description"
                required
                rows={4}
                value={formData.description}
                onChange={handleChange}
                className="input resize-none"
                placeholder="Describe your product..."
              />
            </div>

            <div>
              <label htmlFor="category" className="label">
                Category *
              </label>
              <select
                id="category"
                name="category"
                required
                value={formData.category}
                onChange={handleChange}
                className="input"
              >
                <option value="">Select a category</option>
                {categories.map((cat) => (
                  <option key={cat} value={cat}>
                    {cat}
                  </option>
                ))}
              </select>
            </div>
          </div>
        </div>

        {/* Pricing & Inventory */}
        <div>
          <h3 className="text-lg font-semibold text-gray-900 mb-4">Pricing & Inventory</h3>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label htmlFor="price" className="label">
                Price ($) *
              </label>
              <input
                id="price"
                name="price"
                type="number"
                required
                min="0.01"
                step="0.01"
                value={formData.price || ''}
                onChange={handleChange}
                className="input"
                placeholder="0.00"
              />
            </div>

            <div>
              <label htmlFor="stock" className="label">
                Stock Quantity *
              </label>
              <input
                id="stock"
                name="stock"
                type="number"
                required
                min="0"
                value={formData.stock || ''}
                onChange={handleChange}
                className="input"
                placeholder="0"
              />
            </div>
          </div>
        </div>

        {/* Images */}
        <div>
          <h3 className="text-lg font-semibold text-gray-900 mb-4">Product Images</h3>
          <div className="space-y-4">
            <div className="flex gap-2">
              <input
                type="url"
                value={imageUrl}
                onChange={(e) => setImageUrl(e.target.value)}
                className="input flex-1"
                placeholder="Enter image URL"
              />
              <button
                type="button"
                onClick={addImage}
                className="btn btn-secondary"
              >
                <Plus className="h-4 w-4" />
              </button>
            </div>

            {formData.images && formData.images.length > 0 && (
              <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                {formData.images.map((img, index) => (
                  <div key={index} className="relative group">
                    <img
                      src={img}
                      alt={`Product ${index + 1}`}
                      className="w-full aspect-square object-cover rounded-lg"
                    />
                    <button
                      type="button"
                      onClick={() => removeImage(index)}
                      className="absolute top-2 right-2 p-1 bg-red-500 text-white rounded-full opacity-0 group-hover:opacity-100 transition-opacity"
                    >
                      <X className="h-4 w-4" />
                    </button>
                  </div>
                ))}
              </div>
            )}

            <p className="text-sm text-gray-500">
              Add image URLs for your product. First image will be the main image.
            </p>
            <div className="text-xs text-gray-400 mt-2">
              <p className="font-medium mb-1">Sample image URLs you can use:</p>
              <ul className="list-disc list-inside space-y-1">
                <li>https://picsum.photos/400/400?random=1</li>
                <li>https://placehold.co/400x400/e2e8f0/64748b?text=Product</li>
              </ul>
            </div>
          </div>
        </div>

        {/* Submit */}
        <div className="flex gap-4 pt-4">
          <button
            type="button"
            onClick={() => navigate('/vendor/dashboard')}
            className="btn btn-secondary flex-1"
          >
            Cancel
          </button>
          <button
            type="submit"
            disabled={mutation.isPending}
            className="btn btn-primary flex-1"
          >
            {mutation.isPending ? (
              <LoadingSpinner size="sm" className="text-white" />
            ) : (
              'Create Product'
            )}
          </button>
        </div>

        <p className="text-sm text-gray-500 text-center">
          Your product will be reviewed by an admin before it becomes visible to customers.
        </p>
      </form>
    </div>
  );
}
