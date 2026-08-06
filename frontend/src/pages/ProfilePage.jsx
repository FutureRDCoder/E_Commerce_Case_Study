import { useProfile } from "../hooks/useProfile";

function ProfilePage() {

  const {
    data: user,
    isLoading,
    isError,
    error,
  } = useProfile();

  if (isLoading) {
    return (
      <div className="py-16 text-center text-slate-400">
        Loading profile...
      </div>
    );
  }

  if (isError) {
    return (
      <div className="py-16 text-center text-red-400">
        {error.message}
      </div>
    );
  }

  return (

    <section className="mx-auto max-w-3xl space-y-8">

      <div>

        <p className="text-sm font-semibold uppercase tracking-widest text-primary-400">
          Account
        </p>

        <h1 className="text-3xl font-bold text-white sm:text-4xl">
          My Profile
        </h1>

        <p className="mt-2 text-slate-400">
          Your account information.
        </p>

      </div>

      <div className="card p-4 sm:p-8">

        <div className="grid gap-5 sm:grid-cols-2">

          <div>
            <h2 className="font-semibold text-slate-500">
              Name
            </h2>

            <p className="mt-1 text-lg text-white">
              {user.name}
            </p>
          </div>

          <div>
            <h2 className="font-semibold text-slate-500">
              Username
            </h2>

            <p className="mt-1 text-lg text-white">
              {user.username}
            </p>
          </div>

          <div>
            <h2 className="font-semibold text-slate-500">
              Email
            </h2>

            <p className="mt-1 text-lg text-white">
              {user.email}
            </p>
          </div>

          <div>
            <h2 className="font-semibold text-slate-500">
              Role
            </h2>

            <p className="mt-1 text-lg text-white">
              {user.role}
            </p>
          </div>

          {user.tenantName && (

            <div>

              <h2 className="font-semibold text-slate-500">
                Brand
              </h2>

              <p className="mt-1 text-lg text-white">
                {user.tenantName}
              </p>

            </div>

          )}

        </div>

      </div>

    </section>

  );

}

export default ProfilePage;
