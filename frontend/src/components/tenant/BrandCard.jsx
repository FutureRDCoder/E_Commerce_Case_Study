import { Link } from "react-router-dom";

function BrandCard({ tenant }) {

  return (

    <Link
      to={`/${tenant.slug}/products`}
      className="
        card
        card-hover
        p-6
        text-center
      "
    >

      <img
        src={tenant.logoUrl}
        alt={tenant.name}
        className="
          mx-auto
          mb-5
          h-24
          w-24
          rounded-full
          border
          border-white/10
          object-cover
          shadow-card
        "
      />

      <h2
        className="
          font-display
          text-xl
          font-semibold
          text-white
        "
      >
        {tenant.name}
      </h2>

      <p
        className="
          mt-2
          text-sm
          text-slate-400
        "
      >
        {tenant.description}
      </p>

    </Link>

  );

}

export default BrandCard;
