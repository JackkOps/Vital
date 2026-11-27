import * as React from "react";
import * as LabelPrimitive from "@radix-ui/react-label";
import { cn } from "@/lib/utils";

function Label({ className, ...props }: React.ComponentProps<typeof LabelPrimitive.Root>) {
  return (
    <LabelPrimitive.Root
      className={cn("block text-[12.5px] font-medium text-ink-muted", className)}
      {...props}
    />
  );
}

/** Mesmo visual do Label para grupos que não têm um único controle associado. */
function FieldLegend({ className, ...props }: React.HTMLAttributes<HTMLParagraphElement>) {
  return <p className={cn("text-[12.5px] font-medium text-ink-muted", className)} {...props} />;
}

export { Label, FieldLegend };
