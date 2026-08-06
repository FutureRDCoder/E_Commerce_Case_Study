import { useEffect, useRef } from 'react';
import toast from 'react-hot-toast';
import { Crown } from 'lucide-react';

import { useNotifications } from '../../hooks/useNotifications';

function NotificationAlert() {
  const { data: notifications = [] } = useNotifications();

  const seenIds = useRef(new Set());

  useEffect(() => {
    if (!notifications || notifications.length === 0) {
      return;
    }

    notifications.forEach((notification) => {
      if (
        notification.read ||
        seenIds.current.has(notification.id)
      ) {
        return;
      }

      seenIds.current.add(notification.id);

      toast(
        (t) => (
          <div className="flex items-start gap-3">
            <div className="bg-gradient-brand flex h-10 w-10 shrink-0 items-center justify-center rounded-full shadow-glow">
              <Crown className="h-5 w-5 text-white" />
            </div>

            <div className="min-w-0 flex-1">
              <p className="text-sm font-semibold text-white">
                TENANT ADMIN
              </p>

              <p className="mt-0.5 text-sm text-slate-400">
                {notification.message}
              </p>
            </div>

            <button
              onClick={() => toast.dismiss(t.id)}
              className="text-xs text-slate-500 transition-colors hover:text-white"
            >
              Dismiss
            </button>
          </div>
        ),
        { duration: 8000 }
      );
    });
  }, [notifications]);

  return null;
}

export default NotificationAlert;
