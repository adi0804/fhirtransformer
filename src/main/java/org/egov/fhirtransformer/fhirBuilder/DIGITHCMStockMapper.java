package org.egov.fhirtransformer.fhirBuilder;

import ca.uhn.fhir.context.FhirContext;
import org.egov.common.models.stock.*;
import org.egov.fhirtransformer.common.Constants;
import org.hl7.fhir.r5.model.*;
import java.util.UUID;
import java.util.Date;

/**
 * Utility to map Stock API search data to FHIR InventoryItem resources.
 */
public class DIGITHCMStockMapper {

    public static SupplyDelivery buildSupplyDeliveryFromStock(Stock stock) {

        SupplyDelivery supplyDelivery = new SupplyDelivery();
        supplyDelivery.setId(stock.getId());
        Identifier identifier = new Identifier()
                .setSystem(Constants.IDENTIFIER_SYSTEM_WAYBILL)
                .setValue(stock.getWayBillNumber());
        supplyDelivery.addIdentifier(identifier);

        Long dateOfEntry = stock.getDateOfEntry();
        DateType dateOfEntryDt = (dateOfEntry != null) ? new DateType(String.valueOf(dateOfEntry)) : null;
        supplyDelivery.setOccurrence(dateOfEntryDt);

        SupplyDelivery.SupplyDeliverySuppliedItemComponent suppliedItemComponent =
                new SupplyDelivery.SupplyDeliverySuppliedItemComponent();

        Quantity stockquantity = new Quantity()
                .setValue(stock.getQuantity());
        suppliedItemComponent.setQuantity(stockquantity);
        suppliedItemComponent.setItem(
                new Reference()
                        .setIdentifier(new Identifier()
                                .setSystem(Constants.PRODUCT_VARIANT_IDENTIFIER_SYSTEM)
                                .setValue(stock.getProductVariantId()))
        );

        //Set extension for Supply Delivery Condition
        suppliedItemComponent.addExtension(new Extension().setUrl(Constants.SD_CONDITION_URL)
                .setValue(new CodeableConcept().addCoding(
                        new Coding()
                                .setSystem(Constants.TRANSACTION_REASON_SYSTEM)
                                .setCode(String.valueOf(stock.getTransactionReason())))));
        supplyDelivery.addSuppliedItem(suppliedItemComponent);

        // Set extension for Supply Delivery Stage
        Extension stageExt = new Extension().setUrl(Constants.SD_STAGE_URL)
                .setValue(new CodeableConcept().addCoding(
                        new Coding()
                        .setSystem(Constants.TRANSACTION_TYPE_SYSTEM)
                        .setCode(String.valueOf(stock.getTransactionType()))));
        supplyDelivery.addExtension(stageExt);

        // Set extension for Event Location
        Extension eventLocationExt = new Extension().setUrl(Constants.EVENT_LOCATION_URL)
                .setValue(new Reference()
                        .setIdentifier( new Identifier()
                                .setSystem(Constants.FACILITY_ID_SYSTEM)));
                                //.setValue(stock.getFacilityID())));
        supplyDelivery.addExtension(eventLocationExt);

        // Set extension for Supply Delivery Sender Location
        Extension senderLocationExt = new Extension()
                .setUrl(Constants.SD_SENDER_LOCATION_URL)
                .setValue(new Reference().setIdentifier(
                        new Identifier()
                                .setSystem(Constants.FACILITY_ID_SYSTEM)
                                .setValue(stock.getSenderId())));

        supplyDelivery.addExtension(senderLocationExt);

        supplyDelivery.setDestination(new Reference()
                        .setIdentifier( new Identifier()
                                .setSystem(Constants.FACILITY_ID_SYSTEM)));
                                //.setValue(stock.getFacilityID())));

        return supplyDelivery;
    }

    public static InventoryReport buildInventoryReportFromStockReconciliation(StockReconciliation stockReconciliation) {

        InventoryReport inventoryReport = new InventoryReport();

        inventoryReport.setId(stockReconciliation.getId());
        inventoryReport.setStatus(InventoryReport.InventoryReportStatus.ACTIVE);
        inventoryReport.setCountType(InventoryReport.InventoryCountType.SNAPSHOT);

        Long reportedDateEpoch = stockReconciliation.getDateOfReconciliation();
        Date reportedDate = new Date(reportedDateEpoch);
        inventoryReport.setReportedDateTimeElement(new DateTimeType(reportedDate));

        InventoryReport.InventoryReportInventoryListingComponent listing = new InventoryReport.InventoryReportInventoryListingComponent();
        listing.setCountingDateTime(reportedDate);

        Reference locationRef = new Reference()
                .setType(Constants.LOCATION)
                .setIdentifier(new Identifier()
                        .setSystem(Constants.FACILITY_ID_SYSTEM)
                        .setValue(stockReconciliation.getFacilityId()));

        listing.setLocation(locationRef);

        InventoryReport.InventoryReportInventoryListingItemComponent item= new InventoryReport.InventoryReportInventoryListingItemComponent();

        Quantity qty = new Quantity().setValue(stockReconciliation.getCalculatedCount());
        item.setQuantity(qty);

        Reference itemRef = new Reference().setType(Constants.INVENTORY_ITEM)
                .setIdentifier(new Identifier()
                        .setSystem(Constants.IDENTIFIER_SYSTEM_PV)
                        .setValue(stockReconciliation.getProductVariantId()));
        CodeableReference CodeableReferenceItemRef = new CodeableReference(itemRef);
        item.setItem(CodeableReferenceItemRef);

        listing.addItem(item);
        inventoryReport.addInventoryListing(listing);

        return inventoryReport;
    }


    public static Stock buildStockFromSupplyDelivery(SupplyDelivery supplyDelivery) {
        // Implementation for reverse mapping if needed
        Stock stock = new Stock();
        stock.setTenantId(Constants.TENANT_ID);

        //Defaulting the values for mandatory fields
        stock.setSenderType(SenderReceiverType.WAREHOUSE);
        stock.setReferenceIdType(ReferenceIdType.OTHER);
        stock.setReceiverType(SenderReceiverType.WAREHOUSE);

        stock.setId(supplyDelivery.getIdElement().getIdPart());
        for (Identifier identifier : supplyDelivery.getIdentifier()) {
            if (Constants.IDENTIFIER_SYSTEM_WAYBILL.equals(identifier.getSystem())) {
                stock.setWayBillNumber(identifier.getValue());
            }
        }

        if (supplyDelivery.hasOccurrenceDateTimeType()) {
            stock.setDateOfEntry(supplyDelivery.getOccurrenceDateTimeType().getValue().getTime());
        }

        for (SupplyDelivery.SupplyDeliverySuppliedItemComponent suppliedItem : supplyDelivery.getSuppliedItem()) {
            if (suppliedItem.hasItemReference() && suppliedItem.getItemReference().hasIdentifier()) {
                Identifier itemId = suppliedItem.getItemReference().getIdentifier();
                if (Constants.PRODUCT_VARIANT_IDENTIFIER_SYSTEM.equals(itemId.getSystem())) {
                    stock.setProductVariantId(itemId.getValue());
                }
            }

            // quantity → Stock.quantity
            Quantity qty = suppliedItem.getQuantity();
            if (qty != null && qty.hasValue()) {
                stock.setQuantity(qty.getValue().intValue());
            }

            // suppliedItem.extension where url is supplydelivery-condition → Stock.transactionReason
            for (Extension ext : suppliedItem.getExtension()) {
                if (Constants.SD_CONDITION_URL.equals(ext.getUrl())) {
                    CodeableConcept cc = (CodeableConcept) ext.getValue();
                    if (cc != null && cc.hasCoding()) {
                        if (Constants.TRANSACTION_REASON_SYSTEM.equals(cc.getCodingFirstRep().getSystem())) {
                            // transaction reason is a enum
                            stock.setTransactionReason(TransactionReason.fromValue(cc.getCodingFirstRep().getCode()));
                        }
                    }
                }
            }

            if (supplyDelivery.hasDestination() && supplyDelivery.getDestination().hasIdentifier()) {
                Identifier destId = supplyDelivery.getDestination().getIdentifier();
                if (Constants.FACILITY_ID_SYSTEM.equals(destId.getSystem())) {
                    stock.setReceiverId(destId.getValue());
                }
            }

            for (Extension ext : supplyDelivery.getExtension()) {
                switch (ext.getUrl()) {
                    case Constants.SD_STAGE_URL:
                        CodeableConcept ccStage = (CodeableConcept) ext.getValue();
                        if (ccStage != null && ccStage.hasCoding()) {
                            if (Constants.TRANSACTION_TYPE_SYSTEM.equals(ccStage.getCodingFirstRep().getSystem())) {
                                stock.setTransactionType(TransactionType.fromValue(ccStage.getCodingFirstRep().getCode()));
                            }
                        }
                        break;

                    case Constants.EVENT_LOCATION_URL:
                        Reference eventLoc = (Reference) ext.getValue();
                        if (eventLoc != null && eventLoc.hasIdentifier()) {
                            Identifier id = eventLoc.getIdentifier();
                            if (Constants.FACILITY_ID_SYSTEM.equals(id.getSystem())) {
                                stock.setReferenceId(id.getValue()); //change it facilityID once added
                            }
                        }
                        break;

                    case Constants.SD_SENDER_LOCATION_URL:
                        Reference senderLoc = (Reference) ext.getValue();
                        if (senderLoc != null && senderLoc.hasIdentifier()) {
                            Identifier id = senderLoc.getIdentifier();
                            if (Constants.FACILITY_ID_SYSTEM.equals(id.getSystem())) {
                                stock.setSenderId(id.getValue());
                            }
                        }
                        break;
                }
            }
        }
        return stock;
    }

}
