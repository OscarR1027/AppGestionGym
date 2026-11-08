import { pgTable, serial, text, integer, numeric, date, boolean, primaryKey, check } from 'drizzle-orm/pg-core';
import { sql } from 'drizzle-orm';
export const users = pgTable('gym_users', {
  id: serial().primaryKey(), name: text().notNull(), mail: text().notNull().unique(),
  address: text().notNull(), phoneNumber: text('phone_number').notNull(), documentNumber: text('document_number').notNull().unique(),
  photoData: text('photo_data'), physicalConditionReport: text('physical_condition_report'), role: text().notNull().default('USER'),
});
export const coaches = pgTable('gym_coaches', { idCoach: serial('id_coach').primaryKey(), nameCoach: text('name_coach').notNull(), photoData: text('photo_data') });
export const services = pgTable('gym_services', { idService: serial('id_service').primaryKey(), serviceName: text('service_name').notNull() });
export const plans = pgTable('gym_plans', { idTypePlan: serial('id_type_plan').primaryKey(), namePlan: text('name_plan').notNull(), value: numeric({ precision: 14, scale: 2, mode: 'number' }).notNull() }, t => [check('plan_value_nonnegative', sql`${t.value} >= 0`)]);
export const planServices = pgTable('gym_plan_services', {
  planId: integer('plan_id').notNull().references(() => plans.idTypePlan, { onDelete: 'cascade' }),
  serviceId: integer('service_id').notNull().references(() => services.idService),
}, t => [primaryKey({ columns: [t.planId, t.serviceId] })]);
export const products = pgTable('gym_products', {
  id: serial().primaryKey(), nameProduct: text('name_product').notNull(), value: numeric({ precision: 14, scale: 2, mode: 'number' }).notNull(), amount: integer().notNull(), photoData: text('photo_data'),
}, t => [check('product_stock_nonnegative', sql`${t.amount} >= 0`), check('product_value_nonnegative', sql`${t.value} >= 0`)]);
export const affiliations = pgTable('gym_affiliations', {
  id: serial().primaryKey(), userId: integer('user_id').notNull().references(() => users.id),
  typePlanId: integer('type_plan_id').notNull().references(() => plans.idTypePlan),
  affiliationDate: date('affiliation_date').notNull(), endAffiliation: date('end_affiliation').notNull(),
  paidValue: numeric('paid_value', { precision: 14, scale: 2, mode: 'number' }).notNull(), paymentMethod: text('payment_method').notNull(),
  renewal: boolean().notNull().default(false), invoiceCancelled: boolean('invoice_cancelled').notNull().default(false),
}, t => [check('affiliation_dates_ordered', sql`${t.endAffiliation} >= ${t.affiliationDate}`)]);
export const affiliationCoaches = pgTable('gym_affiliation_coaches', {
  affiliationId: integer('affiliation_id').notNull().references(() => affiliations.id, { onDelete: 'cascade' }),
  coachId: integer('coach_id').notNull().references(() => coaches.idCoach), startDate: date('start_date').notNull(), endDate: date('end_date').notNull(),
}, t => [primaryKey({ columns: [t.affiliationId, t.coachId] })]);
export const sales = pgTable('gym_sales', {
  id: serial().primaryKey(), userId: integer('user_id').notNull().references(() => users.id), saleDate: date('sale_date').notNull(),
  totalValue: numeric('total_value', { precision: 14, scale: 2, mode: 'number' }).notNull(), paymentMethod: text('payment_method').notNull(),
});
export const saleProducts = pgTable('gym_sale_products', {
  saleId: integer('sale_id').notNull().references(() => sales.id, { onDelete: 'cascade' }),
  productId: integer('product_id').notNull().references(() => products.id), productName: text('product_name').notNull(),
  amount: integer().notNull(), unitPrice: numeric('unit_price', { precision: 14, scale: 2, mode: 'number' }).notNull(),
  subtotal: numeric({ precision: 14, scale: 2, mode: 'number' }).notNull(),
}, t => [primaryKey({ columns: [t.saleId, t.productId] }), check('sale_amount_positive', sql`${t.amount} > 0`)]);
