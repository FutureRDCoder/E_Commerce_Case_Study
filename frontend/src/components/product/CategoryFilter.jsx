function CategoryFilter({
  value,
  onChange,
  categories = [],
}) {
  return (
    <select
      value={value}
      onChange={onChange}
      className="input cursor-pointer"
    >
      <option value="" className="bg-night-800">
        All Categories
      </option>

      {categories.map((category) => (
        <option
          key={category}
          value={category}
          className="bg-night-800"
        >
          {category}
        </option>
      ))}
    </select>
  );
}

export default CategoryFilter;
