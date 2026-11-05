# Forms

HTML forms post `application/x-www-form-urlencoded` or `multipart/form-data` data. mangoo I/O parses that into a **`Form`** object you inject directly as a controller parameter. `Form` extends **`Validator`**, so the same method can read fields and run validation rules before you persist or redirect, no separate validator object to wire up.

On **GET**, you receive an empty `Form`. On **POST**, **PUT**, and **PATCH**, the body is parsed, unless the request still carries a kept-form cookie from the previous redirect. In that case the restored form is used and the new body is not parsed. File uploads are `Optional<byte[]>` per field name, not `java.io.File`.

Pass `io.mangoo.routing.bindings.Form` into a controller method:

```java
public Response save(Form form) {
    String firstname = form.get("firstname");
    return Response.ok().render();
}
```

Typed getters:

```java
form.get("firstname");               // null if the key is absent
form.getValue("firstname");           // String, empty if missing
form.getString("firstname");          // Optional<String>
form.getInteger("age");               // Optional<Integer>
form.getLong("id");
form.getDouble("amount");
form.getFloat("ratio");
form.getBoolean("active");
form.getFile("resume");               // Optional<byte[]>
```

!!! note
    Form fields are parsed for `POST`, `PUT`, and `PATCH`. On `GET` you still receive a `Form` instance, but it has no submitted values.

The form is also available in templates without passing it explicitly from the controller.

Keep values across a redirect, typically right after a validation error so the user does not have to retype everything:

```java
form.keep();
```

`form.discard()` clears the current values and file bytes. It does not clear validation errors, and it does not turn off `keep()`. The framework calls it after the response is written, to drop the file bytes. Calling it in the controller before `return`, after `keep()`, still stores the form, now with empty fields. To stop a keep, do not call `keep()`.

!!! note
    Only submitted values and validation errors survive the redirect. **Uploaded files are never part of the flash scope**, since the flash scope is a cookie and file content has no business travelling through a signed and encrypted HTTP header. After a redirect, `getFile(...)` returns an empty `Optional`, so a user has to re-select the file.

    The size check runs on the finished cookie, after signing and encryption. If name plus value exceed 4096 bytes, the kept form is dropped and a warning is logged. Flash values you set yourself are only warned about. They are not dropped.

    Budget in practice: the serialization carries only your submitted values and the validation errors, with a fixed overhead of well under 100 bytes, which leaves roughly 1800 bytes of actual form data within the 4096 byte cookie. That covers ordinary forms comfortably, but a form with many or very long fields can still be dropped, so watch the log for the warning rather than assuming a kept form always survives the redirect.

Default upload limits: 10 files, 1000 parameters, 10 000 characters per value, and a field name of at most 200 characters. None of those four are configurable. `form.maxfilesize` defaults to 5 MiB. The HTTP body is separately limited by `undertow.maxentitysize` (4 MiB by default), so the 5 MiB file default cannot be reached until that is raised. A violation is an `IOException`. The client gets HTTP 400. An HTML response still uses the built-in 500 page. A JSON content type gets `{"error":"<reason phrase>"}`. `addError` keeps the first error per field.

## Validation

```html
<form action="/save" method="post">
    <input type="text" name="firstname" />
    <input type="text" name="lastname" />
    <input type="text" name="email" />
</form>
```

```java
public Response save(Form form) {
    form.expectEmail("email");
    form.expectValue("firstname");
    form.expectValue("lastname");

    if (form.isValid()) {
        // persist
        return Response.redirect("/");
    }

    form.keep();
    return Response.redirect("/save");
}
```

`isValid()` is simply the inverse of `hasErrors()`, use whichever reads better at the call site.

Field checks include:

- `expectValue`, `expectMinLength`, `expectMaxLength`, `expectRangeLength`
- `expectMinValue`, `expectMaxValue`, `expectRangeValue`, `expectNumeric`
- `expectMatch`, `expectExactMatch`, `expectMatch` with a list of allowed values
- `expectEmail`, `expectUrl`, `expectIpv4`, `expectIpv6`, `expectDomainName`, `expectRegex`
- `expectFile`, `expectFileMaxSize`, `expectFileMimeType`
- `expectRegex(String name, Pattern pattern)` takes a `Pattern`, not a `String`

`expectMatch(String name, String message, List<String> values)` and `expectFileMimeType(String name, String message, List<String> allowedMimeTypes)` put the custom message before the list. The other custom-message overloads put the message last. `invalidate()` makes `isValid()` false with an empty-key error. `isSubmitted()` tells a parsed body from an empty form. `addValue` lets a later value win for the same key.

The value checks (`expectMinValue`, `expectMaxValue`, `expectRangeValue`, `expectNumeric`) accept decimal numbers with ASCII digits, an optional sign and a dot as decimal separator, e.g. `-5`, `19.99` or `.5`. Anything else, including Unicode digits, exponents, `NaN` or surrounding whitespace, is reported as a field error. To require digits only, for example for a postal code, use `expectRegex`.

Use these to bind a check to a field name even when the value being checked did not actually come from form input, for example a lookup result you still want reported as a field-level error:

```java
form.expectTrue("username", usernameAvailable);
form.expectFalse("username", usernameTaken);
form.expectNull("token", existing);
form.expectNotNull("user", user);
```

Every `expect*` method also has an overload that takes a custom message, for when the default validation text does not fit.

## Errors in templates

```ftl
<#if form.hasError("firstname")>
    <span class="error">${form.getError("firstname")}</span>
</#if>
```

## Message keys

Override the default messages in `src/main/resources/translations/messages.properties`:

```properties
validation.required={0} is a required value
validation.min.length={0} must be a value with a min length of {1}
validation.max.length={0} must be a value with a max length of {1}
validation.min.value={0} must be a value not less than {1}
validation.max.value={0} must be a value not greater than {1}
validation.exactmatch={0} must exactly match {1}
validation.match={0} must match {1}
validation.matchvalues=The values of {0} is not valid
validation.email={0} must be a valid eMail address
validation.ipv4={0} must be a valid IPv4 address
validation.ipv6={0} must be a valid IPv6 address
validation.range.length={0} must be a length between {1} and {2}
validation.range.value={0} must be value between {1} and {2}
validation.url={0} must be a valid URL
validation.regex={0} is an invalid value
validation.numeric={0} must be a numeric value
validation.domainname={0} must be a valid domain name
validation.mimetype={0} does not have an allowed MimeType
validation.filesize={0} exceeds allowed filesize
validation.file={0} must be a valid file
validation.true={0} must be true
validation.false={0} must be false
validation.null={0} must be empty
validation.notnull={0} is a required value
```

Protect any state-changing form with [CSRF](csrf.md), and use [Flash](flash.md) for one-time messages after the redirect.
