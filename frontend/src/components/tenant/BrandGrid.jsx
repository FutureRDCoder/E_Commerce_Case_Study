import BrandCard from "./BrandCard";

function BrandGrid({
  tenants = [],
}) {

  if (tenants.length === 0) {

    return (
      <div className="py-20 text-center text-slate-500">
        No brands available.
      </div>
    );
  }

  return (

    <div
      className="
        grid
        gap-6
        sm:grid-cols-2
        lg:grid-cols-3
        xl:grid-cols-4
      "
    >

      {tenants.map((tenant) => (

        <BrandCard
          key={tenant.id}
          tenant={tenant}
        />

      ))}

    </div>

  );

}

export default BrandGrid;