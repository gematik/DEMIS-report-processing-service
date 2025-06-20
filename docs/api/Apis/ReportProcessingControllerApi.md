# ReportProcessingControllerApi

All URIs are relative to *http://localhost*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**processReport**](ReportProcessingControllerApi.md#processReport) | **POST** /$process-report |  |


<a name="processReport"></a>
# **processReport**
> String processReport(Content-Type, azp, body, Accept, X-Request-ID, IK-Number, Authorization)



### Parameters

|Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **Content-Type** | [**MediaType**](../Models/.md)|  | [default to null] |
| **azp** | **String**|  | [default to null] |
| **body** | **String**|  | |
| **Accept** | [**MediaType**](../Models/.md)|  | [optional] [default to null] |
| **X-Request-ID** | **String**|  | [optional] [default to null] |
| **IK-Number** | **String**|  | [optional] [default to null] |
| **Authorization** | **String**|  | [optional] [default to null] |

### Return type

**String**

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: application/fhir+json, application/json, application/json+fhir, application/xml
- **Accept**: application/fhir+json, application/json, application/json+fhir, application/xml

