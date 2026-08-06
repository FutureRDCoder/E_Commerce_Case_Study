import { useState } from 'react';
import { Bell, CheckCheck } from 'lucide-react';

import {
  useMarkAllNotificationsAsRead,
  useMarkNotificationAsRead,
  useNotifications,
} from '../../hooks/useNotifications';

function formatTimestamp(value) {
  if (!value) {
    return '';
  }

  const date = new Date(value);

  return date.toLocaleString(undefined, {
    month: 'short',
    day: 'numeric',
    hour: 'numeric',
    minute: '2-digit',
  });
}

function NotificationBell() {
  const [open, setOpen] = useState(false);

  const { data: notifications = [], isLoading } =
    useNotifications();

  const markAsRead = useMarkNotificationAsRead();
  const markAllAsRead = useMarkAllNotificationsAsRead();

  const unreadCount = notifications.filter(
    (notification) => !notification.read
  ).length;

  const handleItemClick = (notification) => {
    if (!notification.read) {
      markAsRead.mutate(notification.id);
    }
    setOpen(false);
  };

  const handleMarkAll = () => {
    markAllAsRead.mutate();
  };

  return (
    <div className="relative">
      <button
        onClick={() => setOpen((value) => !value)}
        aria-label="Notifications"
        className="relative rounded-lg p-2 text-slate-400 transition-colors hover:text-primary-400"
      >
        <Bell className="h-5 w-5" />

        {unreadCount > 0 && (
          <span className="absolute -right-0.5 -top-0.5 flex h-4 min-w-4 items-center justify-center rounded-full bg-pink-500 px-1 text-[10px] font-bold text-white">
            {unreadCount}
          </span>
        )}
      </button>

      {open && (
        <>
          <div
            className="fixed inset-0 z-40"
            onClick={() => setOpen(false)}
          />

          <div className="absolute right-0 z-50 mt-2 w-80 rounded-xl border border-white/10 bg-night-800 shadow-2xl sm:w-96">

            <div className="flex items-center justify-between border-b border-white/10 px-4 py-3">
              <span className="font-semibold text-white">
                Notifications
              </span>

              {unreadCount > 0 && (
                <button
                  onClick={handleMarkAll}
                  disabled={markAllAsRead.isPending}
                  className="flex items-center gap-1 text-xs font-medium text-primary-400 transition-colors hover:text-primary-300"
                >
                  <CheckCheck className="h-3.5 w-3.5" />
                  Mark all as read
                </button>
              )}
            </div>

            <div className="max-h-72 overflow-y-auto">
              {isLoading ? (
                <p className="px-4 py-6 text-center text-sm text-slate-500">
                  Loading notifications...
                </p>
              ) : notifications.length === 0 ? (
                <p className="px-4 py-6 text-center text-sm text-slate-500">
                  No notifications yet.
                </p>
              ) : (
                <ul className="divide-y divide-white/5">
                  {notifications.map((notification) => (
                    <li key={notification.id}>
                      <button
                        onClick={() => handleItemClick(notification)}
                        className="flex w-full items-start gap-3 px-4 py-3 text-left transition-colors hover:bg-white/5"
                      >
                        <span
                          className={`mt-1.5 h-2 w-2 shrink-0 rounded-full ${
                            notification.read
                              ? 'bg-slate-600'
                              : 'bg-pink-500'
                          }`}
                        />

                        <span className="flex-1">
                          <span className="block text-sm text-slate-200">
                            {notification.message}
                          </span>

                          <span className="mt-1 block text-xs text-slate-500">
                            {formatTimestamp(
                              notification.createdAt
                            )}
                          </span>
                        </span>
                      </button>
                    </li>
                  ))}
                </ul>
              )}
            </div>

          </div>
        </>
      )}
    </div>
  );
}

export default NotificationBell;
