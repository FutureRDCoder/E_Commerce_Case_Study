function Footer() {
  return (
    <footer className="border-t border-white/10 bg-night-900/60">

      <div className="mx-auto max-w-7xl px-4 py-8 sm:px-6">

        <div className="flex flex-col items-center justify-between gap-4 sm:flex-row">

          <div className="flex items-center gap-2">
            <div className="bg-gradient-brand rounded-lg p-1.5 text-sm text-white">
              🛍
            </div>

            <span className="font-display font-semibold text-white">
              eCommerce
            </span>
          </div>

          <p className="text-sm text-slate-500">
            © {new Date().getFullYear()} eCommerce. All rights reserved.
          </p>

        </div>

      </div>

    </footer>
  );
}

export default Footer;
