package org.egov.fhirtransformer.fhirBuilder;

import ca.uhn.fhir.context.FhirContext;
import org.egov.common.models.stock.Stock;
import org.egov.common.models.stock.StockReconciliation;
import org.egov.fhirtransformer.common.Constants;
import org.hl7.fhir.r5.model.*;

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

}
