package org.javlo.service.erp.billing;

import java.util.List;

public interface IBillingService {

    /**
     * Create a new invoice
     * @param invoice the invoice to create
     * @return the created invoice with generated id
     */
    Invoice createInvoice(Invoice invoice);

    /**
     * Retrieve an invoice by its id
     * @param id the invoice id
     * @return the invoice or null if not found
     */
    Invoice getInvoiceById(String id);

    /**
     * Get the list of all invoices
     * @return list of invoices
     */
    List<Invoice> getAllInvoices();

    /**
     * Get the list of all customers
     * @return list of customers
     */
    List<Customer> getAllCustomers();

    /**
     * Get the list of all projects
     * @return list of projects
     */
    List<Project> getAllProjects();

    /**
     * Get the list of all registrations
     * @return list of registrations
     */
    List<Registration> getAllRegistrations();

    /**
     * Create/register a new entity (company) for Access Point features
     * This is Step 2 of the Billit.eu Access Point integration process
     * @param entity the entity to register
     * @return the created entity with generated id
     */
    Entity createEntity(Entity entity);

    /**
     * Initiate the identification process for an entity
     * This is Step 3 of the Billit.eu Access Point integration process
     * @param registrationId the registration ID of the entity to identify
     * @param masterPartyId the PartyID of the master account (required in header)
     * @param request the identification request with provider and redirect URL
     * @return the identification response containing the redirect URL
     */
    EntityIdentificationResponse identifyEntity(String registrationId, String masterPartyId, EntityIdentificationRequest request);

    /**
     * Get the integration status for a registration
     * This is Step 4 of the Billit.eu Access Point integration process
     * @param registrationId the registration ID to get the status for
     * @return JSON string containing the integration status information
     */
    String getStatus(String registrationId);

    /**
     * Get the list of all products
     * @return list of products
     */
    List<Product> getProducts();
}
