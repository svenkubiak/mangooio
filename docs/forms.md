# Forms

HTML forms post `application/x-www-form-urlencoded` or `multipart/form-data` data. mangoo I/O parses that into a **`Form`** object you inject directly as a controller parameter. `Form` extends **`Validator`**, so the same method can read fields and run validation rules before you persist or redirect, no separate validator object to wire up.

On **GET**, you receive an empty `Form`, which is handy for rendering default values on the initial page load. On **POST**, **PUT**, and **PATCH**, the body is parsed and available through typed getters. File uploads are exposed as `Optional<byte[]>` per field name, not as `java.io.File`, so there is no temp file to clean up afterward.

Pass `io.mangoo.routing.bindings.Form` into a controller method:

```java
public Response save(Form form) {
    String firstname = form.get("firstname");
    return Response.ok().render();
}
```

Typed getters:

```java
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

Call `form.discard()` to drop kept values once they are no longer needed.

Default upload limits, which are not configurable in `config.yaml`: 10 files, 5 MB per file, 1000 parameters, 10 000 characters per value. The HTTP body as a whole is separately limited by `undertow.maxentitysize` (4 MB by default).

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
```

Protect any state-changing form with [CSRF](csrf.md), and use [Flash](flash.md) for one-time messages after the redirect.
