import React from "react";
import ReactDOM from "react-dom/client";
import { BrowserRouter } from "react-router-dom";
import { QueryClientProvider } from "@tanstack/react-query";
import { Toaster } from "react-hot-toast";

import App from "./App";
import "./index.css";

import queryClient from "./api/queryClient";

ReactDOM.createRoot(document.getElementById("root")).render(
  <React.StrictMode>
    <BrowserRouter>

      <QueryClientProvider client={queryClient}>

        <App />

        <Toaster
          position="top-right"
          reverseOrder={false}
        />

      </QueryClientProvider>

    </BrowserRouter>
  </React.StrictMode>
);