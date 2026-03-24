package org.javlo.service.erp.billing;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Invoice {

    private String id;
    private String invoiceNumber;
    private Date invoiceDate;
    private Date dueDate;
    
    // Customer information
    private String customerId;
    private String customerName;
    private String customerEmail;
    private String customerAddress;
    private String customerCity;
    private String customerZip;
    private String customerCountry;
    private String customerVatNumber;
    
    // Amounts
    private double subtotal;
    private double taxAmount;
    private double totalAmount;
    private String currency;
    
    // Status and payment
    private InvoiceStatus status;
    private String paymentMethod;
    private Date paymentDate;
    
    // Additional information
    private String notes;
    private String reference;
    
    // Invoice lines
    private List<InvoiceLine> lines;

    public Invoice() {
        this.lines = new ArrayList<>();
        this.status = InvoiceStatus.DRAFT;
        this.currency = "EUR";
        this.invoiceDate = new Date();
    }

    public void calculateTotals() {
        this.subtotal = 0.0;
        this.taxAmount = 0.0;
        
        for (InvoiceLine line : lines) {
            this.subtotal += line.getLineSubtotal();
            this.taxAmount += line.getLineTaxAmount();
        }
        
        this.totalAmount = this.subtotal + this.taxAmount;
    }

    public void addLine(InvoiceLine line) {
        this.lines.add(line);
        this.calculateTotals();
    }

    public void removeLine(InvoiceLine line) {
        this.lines.remove(line);
        this.calculateTotals();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public Date getInvoiceDate() {
        return invoiceDate;
    }

    public void setInvoiceDate(Date invoiceDate) {
        this.invoiceDate = invoiceDate;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public String getCustomerAddress() {
        return customerAddress;
    }

    public void setCustomerAddress(String customerAddress) {
        this.customerAddress = customerAddress;
    }

    public String getCustomerCity() {
        return customerCity;
    }

    public void setCustomerCity(String customerCity) {
        this.customerCity = customerCity;
    }

    public String getCustomerZip() {
        return customerZip;
    }

    public void setCustomerZip(String customerZip) {
        this.customerZip = customerZip;
    }

    public String getCustomerCountry() {
        return customerCountry;
    }

    public void setCustomerCountry(String customerCountry) {
        this.customerCountry = customerCountry;
    }

    public String getCustomerVatNumber() {
        return customerVatNumber;
    }

    public void setCustomerVatNumber(String customerVatNumber) {
        this.customerVatNumber = customerVatNumber;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }

    public double getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(double taxAmount) {
        this.taxAmount = taxAmount;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public InvoiceStatus getStatus() {
        return status;
    }

    public void setStatus(InvoiceStatus status) {
        this.status = status;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public Date getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(Date paymentDate) {
        this.paymentDate = paymentDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public List<InvoiceLine> getLines() {
        return lines;
    }

    public void setLines(List<InvoiceLine> lines) {
        this.lines = lines;
        this.calculateTotals();
    }
}
