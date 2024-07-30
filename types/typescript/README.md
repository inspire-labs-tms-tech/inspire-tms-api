# @inspire-labs-tms-tech/inspire-tms-api

## Install

```shell
npm install @inspire-labs-tms-tech/inspire-tms-api
```

## Use

```typescript
import {InspireTMS} from "@inspire-labs-tms-tech/inspire-tms-api";

const slug = "sandbox"; // enter organization slug here
InspireTMS.OpenAPI.BASE = `https://api.${slug}.app.inspiretmsconnect.com`;

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
