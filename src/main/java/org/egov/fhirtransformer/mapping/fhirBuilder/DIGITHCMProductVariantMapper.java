package org.egov.fhirtransformer.mapping.fhirBuilder;

import org.egov.common.models.product.ProductVariant;
import org.egov.fhirtransformer.common.Constants;
import org.hl7.fhir.r5.model.*;
import java.util.UUID;

import java.util.Date;

public class DIGITHCMProductVariantMapper {
//    public static InventoryItem buildInventoryFromProductVariant(ProductVariant productVariant) {
//
//        InventoryItem inventoryItem = new InventoryItem();
//        Long lastModifiedMillis = productVariant.getAuditDetails().getLastModifiedTime();
//        Date lastModified = (lastModifiedMillis != null) ? new Date(lastModifiedMillis) : null;
//
//        Long expiryDateMillis = productVariant.getExpiryDate();
//        Date expiryDate = (expiryDateMillis != null) ? new Date(expiryDateMillis) : null;
//
//        inventoryItem.setId(productVariant.getId());
//        inventoryItem.setStatus(InventoryItem.InventoryItemStatusCodes.ACTIVE);
//
//        // Setting meta information for the Location resource DIGIT HCM Facility profile
//        inventoryItem.setMeta(new Meta()
//                .setLastUpdated(lastModified)
//                .addProfile(Constants.PROFILE_DIGIT_HCM_PV));
//
//        // Adding identifier for facility ID
//        Identifier identifier = new Identifier()
//                .setSystem(Constants.IDENTIFIER_SYSTEM_PV)
//                .setValue(productVariant.getId());
//        Identifier SKUidentifier = new Identifier()
//                .setSystem(Constants.IDENTIFIER_SYSTEM_SKUPV)
//                .setValue(productVariant.getSku());
//
//        inventoryItem.addIdentifier(identifier);
//        inventoryItem.addIdentifier(SKUidentifier);
//
//        // Adding Category
//        inventoryItem.addCategory(new CodeableConcept().addCoding(
//                new Coding()
//                        .setSystem(Constants.CATEGORY_SYSTEM_PV)
//                        .setCode(productVariant.getProduct().getProductType())
//                        .setDisplay(productVariant.getProduct().getProductType())));
//
//        // Adding baseUnit
//        inventoryItem.setBaseUnit(new CodeableConcept().addCoding(
//                new Coding()
//                        .setSystem(Constants.UOM_SYSTEM_PV)
//                        .setCode(productVariant.getBaseUnit())
//                        .setDisplay(productVariant.getBaseUnit())));
//        // Adding NetContent
//        inventoryItem.setNetContent(new Quantity(productVariant.getNetContent()));
//
//        // Adding Name Type
//        InventoryItem.InventoryItemNameComponent nameComponent = new InventoryItem.InventoryItemNameComponent()
//                .setName(productVariant.getVariation())
//                .setLanguage(Enumerations.CommonLanguages.ENUS)
//                .setNameType(new Coding()
//                                .setSystem(Constants.NAMETYPE_SYSTEM_PV)
//                                .setCode(Constants.TRADENAME_PV));
//        inventoryItem.addName(nameComponent);
//
//        InventoryItem.InventoryItemNameComponent productnameComponent = new InventoryItem.InventoryItemNameComponent()
//                .setName(productVariant.getProduct().getProductName())
//                .setLanguage(Enumerations.CommonLanguages.ENUS)
//                .setNameType(new Coding()
//                        .setSystem(Constants.NAMETYPE_SYSTEM_PV)
//                        .setCode(Constants.COMMONNAME_PV));
//        inventoryItem.addName(productnameComponent);
//
//        // Adding Manufacturer as Responsible Organization
//        InventoryItem.InventoryItemResponsibleOrganizationComponent responsibleOrgComponent = new InventoryItem.InventoryItemResponsibleOrganizationComponent()
//                .setOrganization(new Reference().setDisplay(productVariant.getProduct().getProductManufacturer()))
//                .setRole(new CodeableConcept().addCoding(
//                        new Coding()
//                        .setSystem(Constants.RESPORG_SYSTEM_PV)
//                        .setCode(Constants.MANUFACTURER_PV)
//                        .setDisplay(Constants.MANUFACTURER_PV)));
//
//        inventoryItem.addResponsibleOrganization(responsibleOrgComponent);
//
//        //Adding Instance Information
//        InventoryItem.InventoryItemInstanceComponent instanceComponent = new InventoryItem.InventoryItemInstanceComponent()
//                .addIdentifier(new Identifier()
//                .setSystem(Constants.GTIN_PV)
//                .setValue(productVariant.getProduct().getProductGTIN()))
//                .setLotNumber(productVariant.getProduct().getLotNumber())
//                .setExpiry(expiryDate);
//
//        inventoryItem.setInstance(instanceComponent);
//        return inventoryItem;
//    }

    public static InventoryItem buildInventoryFromProductVariant(ProductVariant productVariant) {

        InventoryItem inventoryItem = new InventoryItem();
        Long lastModifiedMillis = productVariant.getAuditDetails().getLastModifiedTime();
        Date lastModified = (lastModifiedMillis != null) ? new Date(lastModifiedMillis) : null;

        Long expiryDateMillis = productVariant.getAuditDetails().getLastModifiedTime();
        Date expiryDate = (expiryDateMillis != null) ? new Date(expiryDateMillis) : null;

        inventoryItem.setId(productVariant.getId());
        inventoryItem.setStatus(InventoryItem.InventoryItemStatusCodes.ACTIVE);


        // Setting meta information for the Location resource DIGIT HCM Facility profile
        inventoryItem.setMeta(new Meta()
                .setLastUpdated(lastModified)
                .addProfile(Constants.PROFILE_DIGIT_HCM_PV));

        // Adding identifier for facility ID
        Identifier Prdctidentifier = new Identifier()
                .setSystem(Constants.IDENTIFIER_SYSTEM_PRDCT)
                .setValue(productVariant.getProductId());
        Identifier identifier = new Identifier()
                .setSystem(Constants.IDENTIFIER_SYSTEM_PV)
                .setValue(productVariant.getId());
        Identifier SKUidentifier = new Identifier()
                .setSystem(Constants.IDENTIFIER_SYSTEM_SKUPV)
                .setValue(productVariant.getSku());

        inventoryItem.addIdentifier(identifier);
        inventoryItem.addIdentifier(SKUidentifier);
        inventoryItem.addIdentifier(Prdctidentifier);

        // Adding Category
        inventoryItem.addCategory(new CodeableConcept().addCoding(
                new Coding()
                        .setSystem(Constants.CATEGORY_SYSTEM_PV)
                        .setCode("Bednet")
                        .setDisplay("Bednet")));

        // Adding baseUnit
        inventoryItem.setBaseUnit(new CodeableConcept().addCoding(
                new Coding()
                        .setSystem(Constants.UOM_SYSTEM)
                        .setCode("bale")
                        .setDisplay("Bale")));
        // Adding NetContent
        inventoryItem.setNetContent(new Quantity(10));

        // Adding Name Type
        InventoryItem.InventoryItemNameComponent nameComponent = new InventoryItem.InventoryItemNameComponent()
                .setName(productVariant.getVariation())
                .setLanguage(Enumerations.CommonLanguages.ENUS)
                .setNameType(new Coding()
                        .setSystem(Constants.NAMETYPE_SYSTEM_PV)
                        .setCode(Constants.TRADENAME_PV));
        inventoryItem.addName(nameComponent);

        InventoryItem.InventoryItemNameComponent productnameComponent = new InventoryItem.InventoryItemNameComponent()
                .setName("Bednet bale")
                .setLanguage(Enumerations.CommonLanguages.ENUS)
                .setNameType(new Coding()
                        .setSystem(Constants.NAMETYPE_SYSTEM_PV)
                        .setCode(Constants.COMMONNAME_PV));
        inventoryItem.addName(productnameComponent);

        // Adding Manufacturer as Responsible Organization
        InventoryItem.InventoryItemResponsibleOrganizationComponent responsibleOrgComponent = new InventoryItem.InventoryItemResponsibleOrganizationComponent()
                .setOrganization(new Reference().setDisplay("FDC Limited"))
                .setRole(new CodeableConcept().addCoding(
                        new Coding()
                                .setSystem(Constants.RESPORG_SYSTEM_PV)
                                .setCode(Constants.MANUFACTURER_PV)
                                .setDisplay(Constants.MANUFACTURER_PV)));

        inventoryItem.addResponsibleOrganization(responsibleOrgComponent);

        //Adding Instance Information
        InventoryItem.InventoryItemInstanceComponent instanceComponent = new InventoryItem.InventoryItemInstanceComponent()
                .addIdentifier(new Identifier()
                        .setSystem(Constants.GTIN_PV)
                        .setValue("00012345600012"))
                .setLotNumber("LN123456")
                .setExpiry(expiryDate);

        inventoryItem.setInstance(instanceComponent);

        return inventoryItem;
    }

    public static ProductVariant buildProductVariantFromInventoryItem(InventoryItem inventoryItem) {
        ProductVariant productVariant = new ProductVariant();
        //Defaulting the values for mandatory fields
        productVariant.setTenantId(Constants.TENANT_ID);
        for (Identifier identifier : inventoryItem.getIdentifier()) {
            String system = identifier.getSystem();
            String value = identifier.getValue();

            System.out.println("System: " + system + ", Value: " + value);
            if (system.equals(Constants.IDENTIFIER_SYSTEM_PRDCT)) {
                productVariant.setProductId(inventoryItem.getIdElement().getId());

            } else if (system.equals(Constants.IDENTIFIER_SYSTEM_SKUPV)) {
                productVariant.setSku(inventoryItem.getIdentifierFirstRep().getValue());
            }
            else if (system.equals(Constants.IDENTIFIER_SYSTEM_PV)) {
                productVariant.setVariation(inventoryItem.getNameFirstRep().getName());
            }
        }

        return productVariant;

    }
}
