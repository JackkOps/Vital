import * as React from "react";
import { cn } from "@/lib/utils";

const fieldClasses =
  "flex h-10 w-full rounded-control border border-border-strong bg-background/60 px-3 text-sm text-ink transition-colors placeholder:text-ink-faint hover:border-ink-faint focus-visible:border-brand focus-visible:bg-surface focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand/20 disabled:opacity-50";

function Input({ className, ...props }: React.InputHTMLAttributes<HTMLInputElement>) {
  return <input className={cn(fieldClasses, className)} {...props} />;
}

function Select({ className, ...props }: React.SelectHTMLAttributes<HTMLSelectElement>) {
  return (
    <select
      className={cn(fieldClasses, "select-chevron cursor-pointer appearance-none pr-8", className)}
      {...props}
    />
  );
}

export { Input, Select, fieldClasses };
