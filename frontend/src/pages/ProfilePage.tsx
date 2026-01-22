import { useNavigate, Link } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { User, Package, Store, LogOut, ChevronRight, Clock } from 'lucide-react';
import { usersApi } from '../api/users';
import { ordersApi } from '../api/orders';
import { useAuthStore } from '../store/authStore';
import { PageLoader } from '../components/common/LoadingSpinner';
import { UserRole, OrderStatus } from '../types';

export function ProfilePage() {
  const navigate = useNavigate();
  const { user, logout, isAuthenticated } = useAuthStore();

  const { data: profile, isLoading: profileLoading } = useQuery({
    queryKey: ['userProfile'],
    queryFn: usersApi.getProfile,
    enabled: isAuthenticated,
  });

  const { data: orders, isLoading: ordersLoading } = useQuery({
    queryKey: ['userOrders'],
    queryFn: () => ordersApi.getUserOrders(0, 5),
    enabled: isAuthenticated,
  });

  const handleLogout = () => {
    logout();
    navigate('/');
  };

  if (!isAuthenticated) {
    navigate('/login');
    return null;
  }

  if (profileLoading) return <PageLoader />;

  const getStatusBadge = (status: OrderStatus) => {
    const statusStyles: Record<string, string> = {
      [OrderStatus.PLACED]: 'badge-info',
      [OrderStatus.CREATED]: 'badge-info',
      [OrderStatus.PREPARING]: 'badge-warning',
      [OrderStatus.SHIPPED]: 'badge-info',
      [OrderStatus.OUT_FOR_DELIVERY]: 'badge-warning',
      [OrderStatus.DELIVERY_SCHEDULED]: 'badge-info',
      [OrderStatus.DELIVERED]: 'badge-success',
      [OrderStatus.CANCELLED]: 'badge-danger',
      // Legacy statuses
      [OrderStatus.PENDING]: 'badge-warning',
      [OrderStatus.CONFIRMED]: 'badge-info',
      [OrderStatus.PROCESSING]: 'badge-info',
    };
    const style = statusStyles[status] || 'badge-info';
    const label = status.replace('_', ' ');
    return <span className={`badge ${style}`}>{label}</span>;
  };

  return (
    <div className="max-w-4xl mx-auto px-4 py-8">
      {/* Profile Header */}
      <div className="card p-6 mb-8">
        <div className="flex items-center space-x-4">
          <div className="w-16 h-16 bg-primary-100 rounded-full flex items-center justify-center">
            <User className="h-8 w-8 text-primary-600" />
          </div>
          <div>
            <h1 className="text-2xl font-bold text-gray-900">
              {profile?.firstName} {profile?.lastName}
            </h1>
            <p className="text-gray-600">{profile?.email}</p>
            <span className="badge badge-info mt-2">{profile?.roles?.join(', ')}</span>
          </div>
        </div>
      </div>

      {/* Quick Actions */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-8">
        <Link
          to="/orders"
          className="card p-4 hover:shadow-md transition-shadow flex items-center space-x-4"
        >
          <div className="w-12 h-12 bg-blue-100 rounded-lg flex items-center justify-center">
            <Package className="h-6 w-6 text-blue-600" />
          </div>
          <div className="flex-1">
            <p className="font-medium text-gray-900">My Orders</p>
            <p className="text-sm text-gray-500">View order history</p>
          </div>
          <ChevronRight className="h-5 w-5 text-gray-400" />
        </Link>

        {user?.roles?.includes(UserRole.USER) && !user?.roles?.includes(UserRole.VENDOR) && (
          <Link
            to="/vendor/register"
            className="card p-4 hover:shadow-md transition-shadow flex items-center space-x-4"
          >
            <div className="w-12 h-12 bg-green-100 rounded-lg flex items-center justify-center">
              <Store className="h-6 w-6 text-green-600" />
            </div>
            <div className="flex-1">
              <p className="font-medium text-gray-900">Become a Vendor</p>
              <p className="text-sm text-gray-500">Start selling</p>
            </div>
            <ChevronRight className="h-5 w-5 text-gray-400" />
          </Link>
        )}

        {user?.roles?.includes(UserRole.VENDOR) && (
          <Link
            to="/vendor/dashboard"
            className="card p-4 hover:shadow-md transition-shadow flex items-center space-x-4"
          >
            <div className="w-12 h-12 bg-green-100 rounded-lg flex items-center justify-center">
              <Store className="h-6 w-6 text-green-600" />
            </div>
            <div className="flex-1">
              <p className="font-medium text-gray-900">Vendor Dashboard</p>
              <p className="text-sm text-gray-500">Manage products</p>
            </div>
            <ChevronRight className="h-5 w-5 text-gray-400" />
          </Link>
        )}

        <button
          onClick={handleLogout}
          className="card p-4 hover:shadow-md transition-shadow flex items-center space-x-4 text-left"
        >
          <div className="w-12 h-12 bg-red-100 rounded-lg flex items-center justify-center">
            <LogOut className="h-6 w-6 text-red-600" />
          </div>
          <div className="flex-1">
            <p className="font-medium text-gray-900">Sign Out</p>
            <p className="text-sm text-gray-500">Log out of your account</p>
          </div>
        </button>
      </div>

      {/* Recent Orders */}
      <div className="card">
        <div className="p-6 border-b flex items-center justify-between">
          <h2 className="text-xl font-semibold text-gray-900">Recent Orders</h2>
          <Link to="/orders" className="text-primary-600 hover:text-primary-700 text-sm font-medium">
            View All
          </Link>
        </div>

        {ordersLoading ? (
          <div className="p-8">
            <PageLoader />
          </div>
        ) : orders?.content.length === 0 ? (
          <div className="p-8 text-center">
            <Package className="h-12 w-12 text-gray-300 mx-auto mb-4" />
            <p className="text-gray-600">No orders yet</p>
            <Link to="/products" className="btn btn-primary mt-4">
              Start Shopping
            </Link>
          </div>
        ) : (
          <div className="divide-y">
            {orders?.content.map((order) => (
              <Link
                key={order.id}
                to={`/orders/${order.id}`}
                className="block p-4 hover:bg-gray-50 transition-colors"
              >
                <div className="flex items-center justify-between">
                  <div>
                    <p className="font-medium text-gray-900">Order #{order.id.slice(-8)}</p>
                    <div className="flex items-center text-sm text-gray-500 mt-1">
                      <Clock className="h-4 w-4 mr-1" />
                      {new Date(order.createdAt).toLocaleDateString()}
                    </div>
                  </div>
                  <div className="text-right">
                    {getStatusBadge(order.status)}
                    <p className="text-lg font-semibold text-gray-900 mt-1">
                      ${order.totalAmount.toFixed(2)}
                    </p>
                  </div>
                </div>
              </Link>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
