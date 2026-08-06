function Pagination({
  page,
  totalPages,
  onPageChange,
}) {
  if (totalPages <= 1) {
    return null;
  }

  return (
    <div className="mt-10 flex flex-wrap items-center justify-center gap-2">

      <button
        onClick={() => onPageChange(page - 1)}
        disabled={page === 0}
        className="btn-secondary px-4 py-2 disabled:cursor-not-allowed disabled:opacity-40"
      >
        Previous
      </button>

      {Array.from(
        { length: totalPages },
        (_, index) => (
          <button
            key={index}
            onClick={() => onPageChange(index)}
            className={`rounded-lg px-4 py-2 transition ${
              page === index
                ? "bg-gradient-brand text-white shadow-glow"
                : "btn-secondary"
            }`}
          >
            {index + 1}
          </button>
        )
      )}

      <button
        onClick={() => onPageChange(page + 1)}
        disabled={page === totalPages - 1}
        className="btn-secondary px-4 py-2 disabled:cursor-not-allowed disabled:opacity-40"
      >
        Next
      </button>

    </div>
  );
}

export default Pagination;
