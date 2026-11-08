CREATE TABLE "gym_affiliation_coaches" (
	"affiliation_id" integer,
	"coach_id" integer,
	"start_date" date NOT NULL,
	"end_date" date NOT NULL,
	CONSTRAINT "gym_affiliation_coaches_pkey" PRIMARY KEY("affiliation_id","coach_id")
);
--> statement-breakpoint
CREATE TABLE "gym_affiliations" (
	"id" serial PRIMARY KEY,
	"user_id" integer NOT NULL,
	"type_plan_id" integer NOT NULL,
	"affiliation_date" date NOT NULL,
	"end_affiliation" date NOT NULL,
	"paid_value" numeric(14,2) NOT NULL,
	"payment_method" text NOT NULL,
	"renewal" boolean DEFAULT false NOT NULL,
	"invoice_cancelled" boolean DEFAULT false NOT NULL,
	CONSTRAINT "affiliation_dates_ordered" CHECK ("end_affiliation" >= "affiliation_date")
);
--> statement-breakpoint
CREATE TABLE "gym_coaches" (
	"id_coach" serial PRIMARY KEY,
	"name_coach" text NOT NULL,
	"photo_data" text
);
--> statement-breakpoint
CREATE TABLE "gym_plan_services" (
	"plan_id" integer,
	"service_id" integer,
	CONSTRAINT "gym_plan_services_pkey" PRIMARY KEY("plan_id","service_id")
);
--> statement-breakpoint
CREATE TABLE "gym_plans" (
	"id_type_plan" serial PRIMARY KEY,
	"name_plan" text NOT NULL,
	"value" numeric(14,2) NOT NULL,
	CONSTRAINT "plan_value_nonnegative" CHECK ("value" >= 0)
);
--> statement-breakpoint
CREATE TABLE "gym_products" (
	"id" serial PRIMARY KEY,
	"name_product" text NOT NULL,
	"value" numeric(14,2) NOT NULL,
	"amount" integer NOT NULL,
	"photo_data" text,
	CONSTRAINT "product_stock_nonnegative" CHECK ("amount" >= 0),
	CONSTRAINT "product_value_nonnegative" CHECK ("value" >= 0)
);
--> statement-breakpoint
CREATE TABLE "gym_sale_products" (
	"sale_id" integer,
	"product_id" integer,
	"product_name" text NOT NULL,
	"amount" integer NOT NULL,
	"unit_price" numeric(14,2) NOT NULL,
	"subtotal" numeric(14,2) NOT NULL,
	CONSTRAINT "gym_sale_products_pkey" PRIMARY KEY("sale_id","product_id"),
	CONSTRAINT "sale_amount_positive" CHECK ("amount" > 0)
);
--> statement-breakpoint
CREATE TABLE "gym_sales" (
	"id" serial PRIMARY KEY,
	"user_id" integer NOT NULL,
	"sale_date" date NOT NULL,
	"total_value" numeric(14,2) NOT NULL,
	"payment_method" text NOT NULL
);
--> statement-breakpoint
CREATE TABLE "gym_services" (
	"id_service" serial PRIMARY KEY,
	"service_name" text NOT NULL
);
--> statement-breakpoint
CREATE TABLE "gym_users" (
	"id" serial PRIMARY KEY,
	"name" text NOT NULL,
	"mail" text NOT NULL UNIQUE,
	"address" text NOT NULL,
	"phone_number" text NOT NULL,
	"document_number" text NOT NULL UNIQUE,
	"photo_data" text,
	"physical_condition_report" text,
	"role" text DEFAULT 'USER' NOT NULL
);
--> statement-breakpoint
ALTER TABLE "gym_affiliation_coaches" ADD CONSTRAINT "gym_affiliation_coaches_affiliation_id_gym_affiliations_id_fkey" FOREIGN KEY ("affiliation_id") REFERENCES "gym_affiliations"("id") ON DELETE CASCADE;--> statement-breakpoint
ALTER TABLE "gym_affiliation_coaches" ADD CONSTRAINT "gym_affiliation_coaches_coach_id_gym_coaches_id_coach_fkey" FOREIGN KEY ("coach_id") REFERENCES "gym_coaches"("id_coach");--> statement-breakpoint
ALTER TABLE "gym_affiliations" ADD CONSTRAINT "gym_affiliations_user_id_gym_users_id_fkey" FOREIGN KEY ("user_id") REFERENCES "gym_users"("id");--> statement-breakpoint
ALTER TABLE "gym_affiliations" ADD CONSTRAINT "gym_affiliations_type_plan_id_gym_plans_id_type_plan_fkey" FOREIGN KEY ("type_plan_id") REFERENCES "gym_plans"("id_type_plan");--> statement-breakpoint
ALTER TABLE "gym_plan_services" ADD CONSTRAINT "gym_plan_services_plan_id_gym_plans_id_type_plan_fkey" FOREIGN KEY ("plan_id") REFERENCES "gym_plans"("id_type_plan") ON DELETE CASCADE;--> statement-breakpoint
ALTER TABLE "gym_plan_services" ADD CONSTRAINT "gym_plan_services_service_id_gym_services_id_service_fkey" FOREIGN KEY ("service_id") REFERENCES "gym_services"("id_service");--> statement-breakpoint
ALTER TABLE "gym_sale_products" ADD CONSTRAINT "gym_sale_products_sale_id_gym_sales_id_fkey" FOREIGN KEY ("sale_id") REFERENCES "gym_sales"("id") ON DELETE CASCADE;--> statement-breakpoint
ALTER TABLE "gym_sale_products" ADD CONSTRAINT "gym_sale_products_product_id_gym_products_id_fkey" FOREIGN KEY ("product_id") REFERENCES "gym_products"("id");--> statement-breakpoint
ALTER TABLE "gym_sales" ADD CONSTRAINT "gym_sales_user_id_gym_users_id_fkey" FOREIGN KEY ("user_id") REFERENCES "gym_users"("id");