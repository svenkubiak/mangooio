# Utils

## Overview

The `io.mangoo.utils` package in the Mangoo I/O framework provides a set of utility classes that simplify various operations. These utilities help with byte processing, encoding, date handling, JSON operations, request handling, and persistence. This documentation outlines the available classes and their functionalities.

---

## **Utility Classes**

### **1. ByteUtils**
**Description:**  
Provides methods for calculating the bit length of byte arrays and strings.

**Key Features:**
- Determine bit length of byte arrays
- Calculate bit length of a given string

---

### **2. CodecUtils**
**Description:**  
Offers encoding and decoding functionalities, including Base64 operations and URL encoding/decoding.

**Key Features:**
- Encode and decode Base64 data
- Perform URL encoding and decoding
- Ensure proper data formatting and transmission

---

### **3. DateUtils**
**Description:**  
Contains methods for converting between `LocalDateTime` and `Date` objects, as well as formatting dates into human-readable strings.

**Key Features:**
- Convert `LocalDateTime` to `Date`
- Convert `Date` to `LocalDateTime`
- Format date values into human-readable strings

---

### **4. JsonUtils**
**Description:**  
Utilizes the Jackson library to provide JSON serialization and deserialization methods.

**Key Features:**
- Convert Java objects to JSON strings
- Deserialize JSON strings into Java objects
- Streamline JSON processing within the application

---

### **5. MangooUtils**
**Description:**  
Offers a collection of general-purpose utility methods, including string manipulation, class loading, and resource handling.

**Key Features:**
- Handle string manipulation efficiently
- Load Java classes dynamically
- Access and manage application resources

---

### **6. PersistenceUtils**
**Description:**  
Manages mappings between class types and their corresponding collection names in the datastore.

**Key Features:**
- Associate class types with collection names
- Facilitate organized data persistence

---

### **7. RequestUtils**
**Description:**  
Provides methods for handling HTTP request data, such as extracting parameters, headers, and cookies.

**Key Features:**
- Extract request parameters
- Retrieve request headers
- Manage HTTP cookies

---

## **Usage Examples**
Each class includes static methods that can be directly accessed without needing to instantiate objects. Below are a few examples:

### Example: Encoding Data with `CodecUtils`
```java
String encoded = CodecUtils.encodeBase64("Hello, Mangoo!");
String decoded = CodecUtils.decodeBase64(encoded);
```

### Example: Formatting Dates with `DateUtils`
```java
String formattedDate = DateUtils.formatDate(LocalDateTime.now());
```

### Example: Handling JSON with `JsonUtils`
```java
String jsonString = JsonUtils.toJson(myObject);
MyObject object = JsonUtils.fromJson(jsonString, MyObject.class);
```