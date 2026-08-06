import { Search } from "lucide-react";

function SearchBar({
  value,
  onChange,
}) {
  return (

    <div className="relative">

      <Search
        className="
          absolute
          left-4
          top-1/2
          h-5
          w-5
          -translate-y-1/2
          text-slate-500
        "
      />

      <input
        type="text"
        placeholder="Search products..."
        value={value}
        onChange={onChange}
        className="input pl-12"
      />

    </div>

  );
}

export default SearchBar;
