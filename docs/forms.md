To access a form submitted to a controller class, you can simply pass the mangoo I/O Form class. Here is an example of how this might look like

```java
public Response index(Form form) {
    ...
}
```

The Form class offers you convenient methods for accessing form values from your template.

```java
public Response index(Form form) {
    File file = form.getFile();
    List = form.getFiles();
    String firstname = form.get("firstname");
    ...
}
```

The Form class is only available if the request is mapped as a POST or PUT method, otherwise the Form class will be null.

The Form class is automatically available in the template so you don’t have to pass the class to your template when working in the template.

By default form values are not passed to a redirected request. In order to keep your form values, you have to call the keep method.

```java
form.keep()
```

This will tell mangoo I/O to store the form values if the request is redirect. This is useful if you have validation errors and want to keep valid form values.

## Form validation

Lets image you have the following form in a template

```HTML
<form method="/save" method="post">
    <input type="text" name="firstname" />
    <input type="text" name="lastname" />
    <input type="text" name="email" />
</form>
```

No lets imagine that you want to validate, that the firstname and lastname from the request is not empty. mangoo I/O offers some convenient functions to validate the submitted values.

```java
public Response form(Form form) {
    form.expectEmail("email");
    form.expectValue("firstname");
    form.expectValue("lastname");

    if (form.isValid()) {
        //Handle form
    }
    ...
}
```

With the form class you can check if a field exists, check an eMail address, etc. The hasErrors\(\) method shows you if the form is valid and can be handled or not.

mangoo I/O supports the following validations out-of-the-box

* Required
* Minimum
* Maximum
* Match \(case-insensitive\)
* Exact match \(case-sensitive\)
* E-Mail
* IPv4
* IPv6
* Range
* Regular expression
* Numeric

There are also additional validation that can be bound to a specific form field but validate a given value rather than the actual form value.

* Validate true
* Validate false
* Validate Null
* Validate not Null

These functions can, e.g., be used to check if a username exists and pass an error message to the appropriate form field.

## Form errors

To show an error in a template, simply check for an error on a spcific field

```
<#if form.hasError("myField")>
```

This is useful if you want to change the CSS style or display an error message when the submitted form is invalid.

To display a form specific error you can use the error method on a form field

```
${form.getError("myField")}
```

This will display e.g.

```
Firstname can not be blank
```

The error messages are defined in your messages.properties file \(or for each language\). There are some default error messages, but they can be overwritten with custom error messages.

If you overwrite a validation message you have to use the appropriate key in your messages file as follows:

```
validation.required={0} is required
validation.min={0} must be at least {1} characters
validation.max={0} can be max {1} characters
validation.exactMatch={0} must exactly match {1}
validation.match={0} must match {1}
validation.email={0} must be a valid eMail address
validation.ipv4={0} must be a valid IPv4 address
validation.ipv6={0} must be a valid IPv6 address
validation.range={0} must be between {1} and {2} characters
validation.url={0} must be a valid URL
validation.regex={0} is invalid
validation.numeric={0} must be an numeric value
```