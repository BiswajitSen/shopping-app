import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { 
  Users, 
  Store, 
  Package, 
  ShoppingCart,
  DollarSign,
  CheckCircle,
  XCircle,
  Clock,
  AlertCircle,
  Eye
} from 'lucide-react';
import { adminApi } from '../../api/admin';
import { useAuthStore } from '../../store/authStore';
import { PageLoader } from '../../components/common/LoadingSpinner';
import { UserRole, VendorStatus, ProductStatus, Vendor, Product } from '../../types';
import toast from 'react-hot-toast';

type TabType = 'overview' | 'vendors' | 'products';

export function AdminDashboardPage() {
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const { user, isAuthenticated } = useAuthStore();
  const [activeTab, setActiveTab] = useState<TabType>('overview');

  const { data: pendingVendors, isLoading: vendorsLoading } = useQuery({
    queryKey: ['pendingVendors'],
    queryFn: () => adminApi.getPendingVendors(0, 50),
    enabled: isAuthenticated && user?.roles?.includes(UserRole.ADMIN),
  });

  const { data: pendingProducts, isLoading: productsLoading } = useQuery({
    queryKey: ['pendingProducts'],
    queryFn: () => adminApi.getPendingProducts(0, 50),
    enabled: isAuthenticated && user?.roles?.includes(UserRole.ADMIN),
  });

  const approveVendorMutation = useMutation({
    mutationFn: adminApi.approveVendor,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['pendingVendors'] });
      toast.success('Vendor approved successfully');
    },
    onError: () => toast.error('Failed to approve vendor'),
  });

  const rejectVendorMutation = useMutation({
    mutationFn: ({ id, reason }: { id: string; reason: string }) => adminApi.rejectVendor(id, reason),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['pendingVendors'] });
      toast.success('Vendor rejected');
    },
    onError: () => toast.error('Failed to reject vendor'),
  });

  const approveProductMutation = useMutation({
    mutationFn: adminApi.approveProduct,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['pendingProducts'] });
      toast.success('Product approved successfully');
    },
    onError: () => toast.error('Failed to approve product'),
  });

  const rejectProductMutation = useMutation({
    mutationFn: ({ id, reason }: { id: string; reason: string }) => adminApi.rejectProduct(id, reason),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['pendingProducts'] });
      toast.success('Product rejected');
    },
    onError: () => toast.error('Failed to reject product'),
  });

  if (!isAuthenticated) {
    return (
      <div className="max-w-4xl mx-auto px-4 py-12 text-center">
        <AlertCircle className="h-16 w-16 text-yellow-500 mx-auto mb-4" />
        <h2 className="text-2xl font-bold text-gray-900 mb-2">Sign In Required</h2>
        <p className="text-gray-600 mb-4">Please sign in to access the admin dashboard.</p>
        <button onClick={() => navigate('/login')} className="btn btn-primary">
          Sign In
        </button>
      </div>
    );
  }

  if (!user?.roles?.includes(UserRole.ADMIN)) {
    return (
      <div className="max-w-4xl mx-auto px-4 py-12 text-center">
        <AlertCircle className="h-16 w-16 text-red-500 mx-auto mb-4" />
        <h2 className="text-2xl font-bold text-gray-900 mb-2">Access Denied</h2>
        <p className="text-gray-600 mb-4">You do not have permission to access this page.</p>
        <button onClick={() => navigate('/')} className="btn btn-primary">
          Go Home
        </button>
      </div>
    );
  }

  const stats = [
    { 
      name: 'Pending Vendors', 
      value: pendingVendors?.totalElements || 0, 
      icon: Store,
      color: 'bg-yellow-500'
    },
    { 
      name: 'Pending Products', 
      value: pendingProducts?.totalElements || 0, 
      icon: Package,
      color: 'bg-orange-500'
    },
  ];

  const tabs = [
    { id: 'overview' as TabType, label: 'Overview' },
    { id: 'vendors' as TabType, label: `Vendors (${pendingVendors?.totalElements || 0})` },
    { id: 'products' as TabType, label: `Products (${pendingProducts?.totalElements || 0})` },
  ];

  const handleApproveVendor = (vendorId: string) => {
    approveVendorMutation.mutate(vendorId);
  };

  const handleRejectVendor = (vendorId: string) => {
    const reason = prompt('Please provide a reason for rejection:');
    if (reason) {
      rejectVendorMutation.mutate({ id: vendorId, reason });
    }
  };

  const handleApproveProduct = (productId: string) => {
    approveProductMutation.mutate(productId);
  };

  const handleRejectProduct = (productId: string) => {
    const reason = prompt('Please provide a reason for rejection:');
    if (reason) {
      rejectProductMutation.mutate({ id: productId, reason });
    }
  };

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      {/* Header */}
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-gray-900">Admin Dashboard</h1>
        <p className="text-gray-600 mt-1">Manage vendors, products, and platform settings</p>
      </div>

      {/* Stats */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mb-8">
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

      {/* Tabs */}
      <div className="border-b mb-6">
        <nav className="flex space-x-8">
          {tabs.map((tab) => (
            <button
              key={tab.id}
              onClick={() => setActiveTab(tab.id)}
              className={`py-4 px-1 border-b-2 font-medium text-sm transition-colors ${
                activeTab === tab.id
                  ? 'border-primary-500 text-primary-600'
                  : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
              }`}
            >
              {tab.label}
            </button>
          ))}
        </nav>
      </div>

      {/* Tab Content */}
      {activeTab === 'overview' && (
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
          {/* Recent Pending Vendors */}
          <div className="card">
            <div className="p-6 border-b">
              <h2 className="text-lg font-semibold text-gray-900">Recent Pending Vendors</h2>
            </div>
            {vendorsLoading ? (
              <div className="p-8"><PageLoader /></div>
            ) : pendingVendors?.content.length === 0 ? (
              <div className="p-8 text-center text-gray-500">
                <CheckCircle className="h-12 w-12 mx-auto mb-2 text-green-500" />
                <p>All vendors have been reviewed</p>
              </div>
            ) : (
              <div className="divide-y">
                {pendingVendors?.content.slice(0, 5).map((vendor) => (
                  <div key={vendor.id} className="p-4 flex items-center justify-between">
                    <div>
                      <p className="font-medium text-gray-900">{vendor.businessName}</p>
                      <p className="text-sm text-gray-500">{vendor.contactEmail}</p>
                    </div>
                    <div className="flex space-x-2">
                      <button
                        onClick={() => handleApproveVendor(vendor.id)}
                        className="p-2 text-green-600 hover:bg-green-50 rounded transition-colors"
                        title="Approve"
                      >
                        <CheckCircle className="h-5 w-5" />
                      </button>
                      <button
                        onClick={() => handleRejectVendor(vendor.id)}
                        className="p-2 text-red-600 hover:bg-red-50 rounded transition-colors"
                        title="Reject"
                      >
                        <XCircle className="h-5 w-5" />
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>

          {/* Recent Pending Products */}
          <div className="card">
            <div className="p-6 border-b">
              <h2 className="text-lg font-semibold text-gray-900">Recent Pending Products</h2>
            </div>
            {productsLoading ? (
              <div className="p-8"><PageLoader /></div>
            ) : pendingProducts?.content.length === 0 ? (
              <div className="p-8 text-center text-gray-500">
                <CheckCircle className="h-12 w-12 mx-auto mb-2 text-green-500" />
                <p>All products have been reviewed</p>
              </div>
            ) : (
              <div className="divide-y">
                {pendingProducts?.content.slice(0, 5).map((product) => (
                  <div key={product.id} className="p-4 flex items-center justify-between">
                    <div className="flex items-center space-x-3">
                      <div className="h-10 w-10 bg-gray-100 rounded overflow-hidden">
                        <img 
                          src={product.images?.[0] || `https://placehold.co/40x40/e2e8f0/64748b?text=${encodeURIComponent(product.name.slice(0, 5))}`} 
                          alt="" 
                          className="h-10 w-10 rounded object-cover"
                          onError={(e) => {
                            e.currentTarget.src = `https://placehold.co/40x40/e2e8f0/64748b?text=${encodeURIComponent(product.name.slice(0, 5))}`;
                          }}
                        />
                      </div>
                      <div>
                        <p className="font-medium text-gray-900">{product.name}</p>
                        <p className="text-sm text-gray-500">${product.price.toFixed(2)}</p>
                      </div>
                    </div>
                    <div className="flex space-x-2">
                      <button
                        onClick={() => handleApproveProduct(product.id)}
                        className="p-2 text-green-600 hover:bg-green-50 rounded transition-colors"
                        title="Approve"
                      >
                        <CheckCircle className="h-5 w-5" />
                      </button>
                      <button
                        onClick={() => handleRejectProduct(product.id)}
                        className="p-2 text-red-600 hover:bg-red-50 rounded transition-colors"
                        title="Reject"
                      >
                        <XCircle className="h-5 w-5" />
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      )}

      {activeTab === 'vendors' && (
        <div className="card">
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead className="bg-gray-50">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Business</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Contact</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Status</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Created</th>
                  <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y">
                {pendingVendors?.content.map((vendor) => (
                  <tr key={vendor.id} className="hover:bg-gray-50">
                    <td className="px-6 py-4">
                      <div>
                        <p className="font-medium text-gray-900">{vendor.businessName}</p>
                        <p className="text-sm text-gray-500 truncate max-w-xs">{vendor.description}</p>
                      </div>
                    </td>
                    <td className="px-6 py-4">
                      <p className="text-sm text-gray-900">{vendor.contactEmail}</p>
                      <p className="text-sm text-gray-500">{vendor.contactPhone || '-'}</p>
                    </td>
                    <td className="px-6 py-4">
                      <span className="badge badge-warning">Pending</span>
                    </td>
                    <td className="px-6 py-4 text-sm text-gray-500">
                      {new Date(vendor.createdAt).toLocaleDateString()}
                    </td>
                    <td className="px-6 py-4 text-right">
                      <div className="flex justify-end space-x-2">
                        <button
                          onClick={() => handleApproveVendor(vendor.id)}
                          className="btn btn-success py-1 px-3 text-sm"
                        >
                          Approve
                        </button>
                        <button
                          onClick={() => handleRejectVendor(vendor.id)}
                          className="btn btn-danger py-1 px-3 text-sm"
                        >
                          Reject
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {activeTab === 'products' && (
        <div className="card">
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead className="bg-gray-50">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Product</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Category</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Price</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Stock</th>
                  <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y">
                {pendingProducts?.content.map((product) => (
                  <tr key={product.id} className="hover:bg-gray-50">
                    <td className="px-6 py-4">
                      <div className="flex items-center space-x-3">
                        <div className="h-12 w-12 bg-gray-100 rounded overflow-hidden">
                          <img 
                            src={product.images?.[0] || `https://placehold.co/48x48/e2e8f0/64748b?text=${encodeURIComponent(product.name.slice(0, 5))}`} 
                            alt="" 
                            className="h-12 w-12 rounded object-cover"
                            onError={(e) => {
                              e.currentTarget.src = `https://placehold.co/48x48/e2e8f0/64748b?text=${encodeURIComponent(product.name.slice(0, 5))}`;
                            }}
                          />
                        </div>
                        <div>
                          <p className="font-medium text-gray-900">{product.name}</p>
                          <p className="text-sm text-gray-500 truncate max-w-xs">{product.description}</p>
                        </div>
                      </div>
                    </td>
                    <td className="px-6 py-4 text-sm text-gray-500">
                      {product.category}
                    </td>
                    <td className="px-6 py-4 text-sm text-gray-900">
                      ${product.price.toFixed(2)}
                    </td>
                    <td className="px-6 py-4 text-sm text-gray-500">
                      {product.stock}
                    </td>
                    <td className="px-6 py-4 text-right">
                      <div className="flex justify-end space-x-2">
                        <button
                          onClick={() => handleApproveProduct(product.id)}
                          className="btn btn-success py-1 px-3 text-sm"
                        >
                          Approve
                        </button>
                        <button
                          onClick={() => handleRejectProduct(product.id)}
                          className="btn btn-danger py-1 px-3 text-sm"
                        >
                          Reject
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  );
}
