import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { 
  Package, 
  ShoppingBag, 
  DollarSign, 
  Clock, 
  Plus, 
  Edit, 
  Trash2,
  AlertCircle,
  CheckCircle,
  XCircle,
  ClipboardList,
  Upload
} from 'lucide-react';
import { vendorsApi } from '../../api/vendors';
import { productsApi } from '../../api/products';
import { useAuthStore } from '../../store/authStore';
import { PageLoader } from '../../components/common/LoadingSpinner';
import { BulkUploadModal } from '../../components/vendor/BulkUploadModal';
import { Product, ProductStatus, VendorStatus, UserRole } from '../../types';
import toast from 'react-hot-toast';

export function VendorDashboardPage() {
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const { user, isAuthenticated } = useAuthStore();
  const [showAddProduct, setShowAddProduct] = useState(false);
  const [showBulkUpload, setShowBulkUpload] = useState(false);

  const { data: vendor, isLoading: vendorLoading } = useQuery({
    queryKey: ['vendorProfile'],
    queryFn: vendorsApi.getProfile,
    enabled: isAuthenticated && user?.roles?.includes(UserRole.VENDOR),
  });

  const { data: products, isLoading: productsLoading } = useQuery({
    queryKey: ['vendorProducts'],
    queryFn: () => productsApi.getVendorProducts(0, 100),
    enabled: isAuthenticated && user?.roles?.includes(UserRole.VENDOR) && vendor?.status === VendorStatus.APPROVED,
  });

  const deleteProductMutation = useMutation({
    mutationFn: productsApi.delete,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['vendorProducts'] });
      toast.success('Product deleted successfully');
    },
    onError: () => {
      toast.error('Failed to delete product');
    },
  });

  if (!isAuthenticated) {
    return (
      <div className="max-w-4xl mx-auto px-4 py-12 text-center">
        <AlertCircle className="h-16 w-16 text-yellow-500 mx-auto mb-4" />
        <h2 className="text-2xl font-bold text-gray-900 mb-2">Sign In Required</h2>
        <p className="text-gray-600 mb-4">Please sign in to access the vendor dashboard.</p>
        <button onClick={() => navigate('/login')} className="btn btn-primary">
          Sign In
        </button>
      </div>
    );
  }

  if (!user?.roles?.includes(UserRole.VENDOR)) {
    return (
      <div className="max-w-4xl mx-auto px-4 py-12 text-center">
        <Package className="h-16 w-16 text-gray-300 mx-auto mb-4" />
        <h2 className="text-2xl font-bold text-gray-900 mb-2">Not a Vendor</h2>
        <p className="text-gray-600 mb-4">
          You need to register as a vendor to access this dashboard.
        </p>
        <Link to="/vendor/register" className="btn btn-primary">
          Become a Vendor
        </Link>
      </div>
    );
  }

  if (vendorLoading) return <PageLoader />;

  if (!vendor) {
    return (
      <div className="max-w-4xl mx-auto px-4 py-12 text-center">
        <AlertCircle className="h-16 w-16 text-yellow-500 mx-auto mb-4" />
        <h2 className="text-2xl font-bold text-gray-900 mb-2">Vendor Profile Not Found</h2>
        <p className="text-gray-600 mb-4">Please complete your vendor registration.</p>
        <Link to="/vendor/register" className="btn btn-primary">
          Complete Registration
        </Link>
      </div>
    );
  }

  if (vendor.status === VendorStatus.PENDING) {
    return (
      <div className="max-w-4xl mx-auto px-4 py-12 text-center">
        <Clock className="h-16 w-16 text-yellow-500 mx-auto mb-4" />
        <h2 className="text-2xl font-bold text-gray-900 mb-2">Application Under Review</h2>
        <p className="text-gray-600 mb-4">
          Your vendor application is being reviewed by our team. 
          You'll be notified once it's approved.
        </p>
        <div className="inline-flex items-center space-x-2 bg-yellow-100 text-yellow-800 px-4 py-2 rounded-lg">
          <Clock className="h-4 w-4" />
          <span>Status: Pending Approval</span>
        </div>
      </div>
    );
  }

  if (vendor.status === VendorStatus.REJECTED) {
    return (
      <div className="max-w-4xl mx-auto px-4 py-12 text-center">
        <XCircle className="h-16 w-16 text-red-500 mx-auto mb-4" />
        <h2 className="text-2xl font-bold text-gray-900 mb-2">Application Rejected</h2>
        <p className="text-gray-600 mb-4">
          Unfortunately, your vendor application was not approved. 
          Please contact support for more information.
        </p>
      </div>
    );
  }

  const stats = [
    { 
      name: 'Total Products', 
      value: products?.totalElements || 0, 
      icon: Package,
      color: 'bg-blue-500'
    },
    { 
      name: 'Pending Approval', 
      value: products?.content.filter(p => p.status === ProductStatus.PENDING).length || 0, 
      icon: Clock,
      color: 'bg-yellow-500'
    },
    { 
      name: 'Active Products', 
      value: products?.content.filter(p => p.status === ProductStatus.APPROVED).length || 0, 
      icon: CheckCircle,
      color: 'bg-green-500'
    },
  ];

  const getStatusBadge = (status: ProductStatus) => {
    switch (status) {
      case ProductStatus.APPROVED:
        return <span className="badge badge-success">Approved</span>;
      case ProductStatus.PENDING:
        return <span className="badge badge-warning">Pending</span>;
      case ProductStatus.REJECTED:
        return <span className="badge badge-danger">Rejected</span>;
      case ProductStatus.OUT_OF_STOCK:
        return <span className="badge badge-danger">Out of Stock</span>;
      default:
        return <span className="badge">{status}</span>;
    }
  };

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      {/* Header */}
      <div className="flex flex-col gap-4 mb-8">
        <div>
          <h1 className="text-2xl sm:text-3xl font-bold text-gray-900">Vendor Dashboard</h1>
          <p className="text-gray-600 mt-1">Welcome back, {vendor.businessName}</p>
        </div>
        <div className="flex flex-wrap gap-2 sm:gap-3">
          <Link to="/vendor/orders" className="btn btn-secondary flex-1 sm:flex-none justify-center">
            <ClipboardList className="h-4 w-4 sm:mr-2" />
            <span className="hidden sm:inline">View Orders</span>
            <span className="sm:hidden">Orders</span>
          </Link>
          <button
            onClick={() => setShowBulkUpload(true)}
            className="btn btn-secondary flex-1 sm:flex-none justify-center"
          >
            <Upload className="h-4 w-4 sm:mr-2" />
            <span className="hidden sm:inline">Bulk Upload</span>
            <span className="sm:hidden">Upload</span>
          </button>
          <Link to="/vendor/products/new" className="btn btn-primary flex-1 sm:flex-none justify-center">
            <Plus className="h-4 w-4 sm:mr-2" />
            <span className="hidden sm:inline">Add New Product</span>
            <span className="sm:hidden">Add</span>
          </Link>
        </div>
      </div>

      {/* Stats */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
        {stats.map((stat, index) => (
          <div key={index} className="card p-6">
            <div className="flex items-center">
              <div className={`${stat.color} p-3 rounded-lg`}>
                <stat.icon className="h-6 w-6 text-white" />
              </div>
              <div className="ml-4">
                <p className="text-sm text-gray-600">{stat.name}</p>
                <p className="text-2xl font-bold text-gray-900">{stat.value}</p>
              </div>
            </div>
          </div>
        ))}
      </div>

      {/* Products List */}
      <div className="card">
        <div className="p-6 border-b">
          <h2 className="text-xl font-semibold text-gray-900">Your Products</h2>
        </div>

        {productsLoading ? (
          <div className="p-12">
            <PageLoader />
          </div>
        ) : products?.content.length === 0 ? (
          <div className="p-12 text-center">
            <Package className="h-16 w-16 text-gray-300 mx-auto mb-4" />
            <h3 className="text-lg font-medium text-gray-900 mb-2">No Products Yet</h3>
            <p className="text-gray-600 mb-4">Start by adding your first product</p>
            <Link to="/vendor/products/new" className="btn btn-primary">
              <Plus className="h-4 w-4 mr-2" />
              Add Product
            </Link>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead className="bg-gray-50">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Product
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Category
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Price
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Stock
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Status
                  </th>
                  <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Actions
                  </th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-200">
                {products?.content.map((product) => (
                  <tr key={product.id} className="hover:bg-gray-50">
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="flex items-center">
                        <div className="h-10 w-10 bg-gray-100 rounded-lg overflow-hidden">
                          <img 
                            src={product.images?.[0] || `https://placehold.co/40x40/e2e8f0/64748b?text=${encodeURIComponent(product.name.slice(0, 5))}`}
                            alt="" 
                            className="h-10 w-10 rounded-lg object-cover"
                            onError={(e) => {
                              e.currentTarget.src = `https://placehold.co/40x40/e2e8f0/64748b?text=${encodeURIComponent(product.name.slice(0, 5))}`;
                            }}
                          />
                        </div>
                        <div className="ml-4">
                          <div className="text-sm font-medium text-gray-900">
                            {product.name}
                          </div>
                        </div>
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                      {product.category}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                      ${product.price.toFixed(2)}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                      {product.stock}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      {getStatusBadge(product.status)}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                      <div className="flex items-center justify-end space-x-2">
                        <Link
                          to={`/vendor/products/${product.id}/edit`}
                          className="p-2 text-gray-600 hover:text-primary-600 hover:bg-gray-100 rounded transition-colors"
                        >
                          <Edit className="h-4 w-4" />
                        </Link>
                        <button
                          onClick={() => {
                            if (confirm('Are you sure you want to delete this product?')) {
                              deleteProductMutation.mutate(product.id);
                            }
                          }}
                          className="p-2 text-gray-600 hover:text-red-600 hover:bg-gray-100 rounded transition-colors"
                        >
                          <Trash2 className="h-4 w-4" />
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Bulk Upload Modal */}
      <BulkUploadModal
        isOpen={showBulkUpload}
        onClose={() => setShowBulkUpload(false)}
      />
    </div>
  );
}
