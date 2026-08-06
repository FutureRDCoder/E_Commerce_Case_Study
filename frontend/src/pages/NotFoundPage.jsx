import { Link } from "react-router-dom";

function NotFoundPage() {
  return (
    <section className="flex flex-col items-center justify-center px-4 py-20 text-center">
      <h1 className="text-gradient text-8xl font-bold">
        404
      </h1>

      <h2 className="mt-4 text-3xl font-bold text-white">
        Not Found!
      </h2>

      <p className="mt-2 text-slate-400">
        The page you are looking for does not exist.
      </p>

      <Link
        to="/products"
        className="btn-primary mt-8 px-6 py-3"
      >
        Back to Products
      </Link>
    </section>
  );
}

export default NotFoundPage;
