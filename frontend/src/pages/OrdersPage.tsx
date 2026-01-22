import { useQuery } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import { Package, Clock, CheckCircle, XCircle, Truck, AlertCircle, Calendar } from 'lucide-react';
import { ordersApi } from '../api/orders';
import { useAuthStore } from '../store/authStore';
import { PageLoader } from '../components/common/LoadingSpinner';
import { OrderStatus } from '../types';

const statusConfig: Record<string, { icon: typeof Clock; color: string; bg: string; label: string }> = {
  [OrderStatus.PLACED]: { icon: Package, color: 'text-blue-600', bg: 'bg-blue-100', label: 'Placed' },
  [OrderStatus.CREATED]: { icon: Package, color: 'text-blue-600', bg: 'bg-blue-100', label: 'Placed' },
  [OrderStatus.PREPARING]: { icon: Clock, color: 'text-yellow-600', bg: 'bg-yellow-100', label: 'Preparing' },
  [OrderStatus.SHIPPED]: { icon: Truck, color: 'text-purple-600', bg: 'bg-purple-100', label: 'Shipped' },
  [OrderStatus.OUT_FOR_DELIVERY]: { icon: Truck, color: 'text-orange-600', bg: 'bg-orange-100', label: 'Out for Delivery' },
  [OrderStatus.DELIVERY_SCHEDULED]: { icon: Calendar, color: 'text-indigo-600', bg: 'bg-indigo-100', label: 'Delivery Scheduled' },
  [OrderStatus.DELIVERED]: { icon: CheckCircle, color: 'text-green-600', bg: 'bg-green-100', label: 'Delivered' },
  [OrderStatus.CANCELLED]: { icon: XCircle, color: 'text-red-600', bg: 'bg-red-100', label: 'Cancelled' },
  // Legacy statuses
  [OrderStatus.PENDING]: { icon: Clock, color: 'text-yellow-600', bg: 'bg-yellow-100', label: 'Pending' },
  [OrderStatus.CONFIRMED]: { icon: CheckCircle, color: 'text-blue-600', bg: 'bg-blue-100', label: 'Confirmed' },
  [OrderStatus.PROCESSING]: { icon: Package, color: 'text-purple-600', bg: 'bg-purple-100', label: 'Processing' },
};

export function OrdersPage() {
  const { isAuthenticated } = useAuthStore();

  const { data: orders, isLoading, error } = useQuery({
    queryKey: ['userOrders'],
    queryFn: () => ordersApi.getUserOrders(0, 50),
    enabled: isAuthenticated,
    // Auto-refresh every 10 seconds to get status updates
    refetchInterval: 10000,
    // Also refetch when window regains focus
    refetchOnWindowFocus: true,
  });

  if (!isAuthenticated) {
    return (
      <div className="max-w-4xl mx-auto px-4 py-12 text-center">
        <Package className="h-16 w-16 text-gray-300 mx-auto mb-4" />
        <h2 className="text-2xl font-bold text-gray-900 mb-2">Sign In Required</h2>
        <p className="text-gray-600 mb-4">Please sign in to view your orders.</p>
        <Link to="/login" className="btn btn-primary">
          Sign In
        </Link>
      </div>
    );
  }

  if (isLoading) {
    return <PageLoader />;
  }

  if (error) {
    return (
      <div className="max-w-4xl mx-auto px-4 py-12 text-center">
        <AlertCircle className="h-16 w-16 text-red-400 mx-auto mb-4" />
        <h2 className="text-2xl font-bold text-gray-900 mb-2">Error Loading Orders</h2>
        <p className="text-gray-600 mb-4">Failed to load your orders. Please try again.</p>
        <button onClick={() => window.location.reload()} className="btn btn-primary">
          Retry
        </button>
      </div>
    );
  }

  const ordersList = orders?.content || [];

  return (
    <div className="max-w-4xl mx-auto px-4 py-8">
      <div className="mb-8">
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-3xl font-bold text-gray-900">My Orders</h1>
            <p className="text-gray-600 mt-1">Track and manage your orders</p>
          </div>
          <div className="flex items-center space-x-2 text-sm text-gray-500">
            <span className="relative flex h-2 w-2">
              <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-green-400 opacity-75"></span>
              <span className="relative inline-flex rounded-full h-2 w-2 bg-green-500"></span>
            </span>
            <span>Live updates</span>
          </div>
        </div>
      </div>

      {ordersList.length === 0 ? (
        <div className="text-center py-12">
          <Package className="h-16 w-16 text-gray-300 mx-auto mb-4" />
          <h2 className="text-xl font-semibold text-gray-900 mb-2">No Orders Yet</h2>
          <p className="text-gray-600 mb-4">
            You haven't placed any orders yet. Start shopping!
          </p>
          <Link to="/products" className="btn btn-primary">
            Browse Products
          </Link>
        </div>
      ) : (
        <div className="space-y-4">
          {ordersList.map((order) => {
            const status = order.status as string;
            const config = statusConfig[status] || statusConfig[OrderStatus.PENDING];
            const StatusIcon = config.icon;

            return (
              <div
                key={order.id}
                className="card hover:shadow-md transition-shadow"
              >
                <div className="p-6">
                  <div className="flex items-center justify-between mb-4">
                    <div>
                      <p className="text-sm text-gray-500">Order ID</p>
                      <p className="font-mono font-medium">{order.id.slice(0, 8)}...</p>
                    </div>
                    <div className={`flex items-center space-x-2 px-3 py-1 rounded-full ${config.bg}`}>
                      <StatusIcon className={`h-4 w-4 ${config.color}`} />
                      <span className={`text-sm font-medium ${config.color}`}>
                        {config.label}
                      </span>
                    </div>
                  </div>

                  <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-4">
                    <div>
                      <p className="text-sm text-gray-500">Date</p>
                      <p className="font-medium">
                        {new Date(order.createdAt).toLocaleDateString()}
                      </p>
                    </div>
                    <div>
                      <p className="text-sm text-gray-500">Items</p>
                      <p className="font-medium">{order.items?.length || 0} items</p>
                    </div>
                    <div>
                      <p className="text-sm text-gray-500">Total</p>
                      <p className="font-medium text-primary-600">
                        ${order.totalAmount?.toFixed(2) || '0.00'}
                      </p>
                    </div>
                    {order.estimatedDeliveryDate && (
                      <div>
                        <p className="text-sm text-gray-500">Est. Delivery</p>
                        <p className="font-medium text-indigo-600">
                          {new Date(order.estimatedDeliveryDate).toLocaleDateString()}
                        </p>
                      </div>
                    )}
                  </div>

                  {/* Status Note */}
                  {order.statusNote && (
                    <div className="mb-4 p-3 bg-gray-50 rounded-lg">
                      <p className="text-sm text-gray-600">
                        <span className="font-medium">Update:</span> {order.statusNote}
                      </p>
                    </div>
                  )}

                  {/* Order Items Preview */}
                  {order.items && order.items.length > 0 && (
                    <div className="border-t pt-4">
                      <div className="flex items-center space-x-4 overflow-x-auto pb-2">
                        {order.items.slice(0, 4).map((item, index) => {
                          const placeholderImage = `https://placehold.co/64x64/e2e8f0/64748b?text=${encodeURIComponent((item.productName || 'Item').slice(0, 5))}`;
                          return (
                            <div
                              key={index}
                              className="flex-shrink-0 w-16 h-16 bg-gray-100 rounded-lg overflow-hidden"
                            >
                              <img
                                src={item.productImage || placeholderImage}
                                alt={item.productName || 'Product'}
                                className="w-full h-full object-cover"
                                onError={(e) => {
                                  e.currentTarget.src = placeholderImage;
                                }}
                              />
                            </div>
                          );
                        })}
                        {order.items.length > 4 && (
                          <div className="flex-shrink-0 w-16 h-16 bg-gray-100 rounded-lg flex items-center justify-center">
                            <span className="text-sm text-gray-500">
                              +{order.items.length - 4}
                            </span>
                          </div>
                        )}
                      </div>
                    </div>
                  )}

                  {/* Shipping Address */}
                  {order.shippingAddress && (
                    <div className="border-t pt-4 mt-4">
                      <p className="text-sm text-gray-500 mb-1">Shipping to:</p>
                      <p className="text-sm">
                        {order.shippingAddress.fullName}, {order.shippingAddress.addressLine1},{' '}
                        {order.shippingAddress.city}, {order.shippingAddress.state}{' '}
                        {order.shippingAddress.postalCode}
                      </p>
                    </div>
                  )}
                </div>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
}
