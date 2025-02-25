# Form Handling in mangoo I/O

To access a submitted form in a controller class, you can pass the mangoo I/O `Form` class as a parameter. Example:

```java
public Response index(Form form) {
    ...
}
```

The `Form` class provides convenient methods to retrieve form values:

```java
public Response index(Form form) {
    File file = form.getFile();
    List<File> files = form.getFiles();
    String firstname = form.get("firstname");
    ...
}
```

### Important Notes
- The `Form` class is only available for `POST` or `PUT` requests; otherwise, it will be `null`.
- The `Form` class is automatically accessible in templates without explicitly passing it.

### Retaining Form Values After Redirects
By default, form values are not retained after a redirect. To persist form values across redirects, use:

```java
form.keep();
```

This is useful when handling validation errors while maintaining previously entered values.

## Form Validation

Consider the following form in a template:

```html
<form action="/save" method="post">
    <input type="text" name="firstname" />
    <input type="text" name="lastname" />
    <input type="text" name="email" />
</form>
```

To validate the `firstname` and `lastname` fields, use the built-in validation functions:

```java
public Response form(Form form) {
    form.expectEmail("email");
    form.expectValue("firstname");
    form.expectValue("lastname");

    if (form.isValid()) {
        // Handle form
    }
    return Response.ok().render();
}
```

The `Form` class allows checks for field existence, email validation, and more. Use `hasErrors()` to determine if the form is valid.

### Built-in Validations
mangoo I/O provides various validation rules:

- **Required**
- **Minimum length**
- **Maximum length**
- **Match (case-insensitive)**
- **Exact match (case-sensitive)**
- **Email format**
- **IPv4 format**
- **IPv6 format**
- **Range check**
- **Regular expressions**
- **Numeric values**

Additionally, validations can be bound to a specific field to check values beyond form input:

- `validateTrue`
- `validateFalse`
- `validateNull`
- `validateNotNull`

These are useful for checking existing usernames or passing custom error messages to form fields.

## Handling Form Errors

To display an error in a template, check for errors in a specific field:

```html
<#if form.hasError("myField")>
```

This is useful for modifying CSS styles or displaying error messages when validation fails.

To retrieve a specific error message:

```html
${form.getError("myField")}
```

For example, it may display:

```
Firstname cannot be blank
```

## Customizing Error Messages
Error messages are defined in `messages.properties` (or language-specific message files). Default messages can be customized as follows:

```properties
validation.required={0} is required
validation.min={0} must be at least {1} characters
validation.max={0} can be a maximum of {1} characters
validation.exactMatch={0} must exactly match {1}
validation.match={0} must match {1}
validation.email={0} must be a valid email address
validation.ipv4={0} must be a valid IPv4 address
validation.ipv6={0} must be a valid IPv6 address
validation.range={0} must be between {1} and {2} characters
validation.url={0} must be a valid URL
validation.regex={0} is invalid
validation.numeric={0} must be a numeric value
```
