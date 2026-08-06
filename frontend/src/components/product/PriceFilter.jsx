export const PRICE_RANGES = {
  all: { minPrice: undefined, maxPrice: undefined },
  "under-100": { minPrice: undefined, maxPrice: 100 },
  "100-500": { minPrice: 100, maxPrice: 500 },
  "500-1000": { minPrice: 500, maxPrice: 1000 },
  "1000-5000": { minPrice: 1000, maxPrice: 5000 },
  "over-5000": { minPrice: 5000, maxPrice: undefined },
};

const PRICE_OPTIONS = [
  { key: "all", label: "All Prices" },
  { key: "under-100", label: "Under ₹100" },
  { key: "100-500", label: "₹100 - ₹500" },
  { key: "500-1000", label: "₹500 - ₹1,000" },
  { key: "1000-5000", label: "₹1,000 - ₹5,000" },
  { key: "over-5000", label: "Over ₹5,000" },
];

function PriceFilter({
  value,
  onChange,
}) {
  return (
    <select
      value={value}
      onChange={onChange}
      aria-label="Price filter"
      className="input cursor-pointer"
    >
      {PRICE_OPTIONS.map((option) => (
        <option
          key={option.key}
          value={option.key}
          className="bg-night-800"
        >
          {option.label}
        </option>
      ))}
    </select>
  );
}

export default PriceFilter;
