# @inspire-labs-tms-tech/inspire-tms-api

## Install

```shell
npm install @inspire-labs-tms-tech/inspire-tms-api
```

## Use

```typescript
import {InspireTMS} from "@inspire-labs-tms-tech/inspire-tms-api";

InspireTMS.createFacility({
  requestBody: {
    displayName: "",
    isActive: true,
    address: {
      line1: "2916 Bluff Road",
      city: "Indianapolis",
      state: "Indiana",
      zipCode: "46225",
    }
  }
});
```
