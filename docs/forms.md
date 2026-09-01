# Forms

Pass `io.mangoo.routing.bindings.Form` into a controller method. `Form` extends `Validator`.

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

The form is also available in templates without passing it explicitly.

Keep values across a redirect (typically after a validation error):

```java
form.keep();
```

Call `form.discard()` to drop kept values.

Default upload limits (not configurable in `config.yaml`): 10 files, 5 MB per file, 1000 parameters, 10 000 characters per value. The HTTP body as a whole is limited by `undertow.maxentitysize` (4 MB by default).

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

`isValid()` is the inverse of `hasErrors()`.

Field checks include:

- `expectValue`, `expectMinLength`, `expectMaxLength`, `expectRangeLength`
- `expectMinValue`, `expectMaxValue`, `expectRangeValue`, `expectNumeric`
- `expectMatch`, `expectExactMatch`, `expectMatch` with a list of allowed values
- `expectEmail`, `expectUrl`, `expectIpv4`, `expectIpv6`, `expectDomainName`, `expectRegex`
- `expectFile`, `expectFileMaxSize`, `expectFileMimeType`

Bind a check to a field for values that are not form input:

```java
form.expectTrue("username", usernameAvailable);
form.expectFalse("username", usernameTaken);
form.expectNull("token", existing);
form.expectNotNull("user", user);
```

Every `expect*` method has an overload that takes a custom message.

## Errors in templates

```ftl
<#if form.hasError("firstname")>
    <span class="error">${form.getError("firstname")}</span>
</#if>
```

## Message keys

Override defaults in `src/main/resources/translations/messages.properties`:

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

Protect state-changing forms with [CSRF](csrf.md). Use [Flash](flash.md) for one-time messages after redirect.
