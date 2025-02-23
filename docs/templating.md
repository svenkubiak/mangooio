## Template variables

There are some default variables which are automatically assigned to the template offering useful functionality out-of-the-box. These are:

```
Form
Flash
Session
i18n
Route
Location
Prettytime
```

Please note, that these variable name can not be passed as such a variable from your controller would overwrite the out-of-the-box ones. If you assign such a variable, the template will not be rendered and result in an exception.

## Pretty Time

Pretty time allows you to format localized relative dates. For instance, if you had a date object that represented yesterday, Pretty time would format that as 1 day ago in the preferred Locale of the request. See the following examples for usage in you templates:

```
${prettytime(localDateTime)} //Based on LocalDateTime
${prettytime(localDate)} //Based on LocalDate
${prettytime(date)} //Based on Date
```

## Subject

Subject allows you to retrieve information about an authentication user if you use the build-in authentication mechanism. See the following examples for usage in you templates:


```
<#if subject.authenticated>
	Hello ${subject.username}!
	//Display navigation for authenticated user
<#else>
	Hello Guest!
	//Display navigation for not authenticated user
</#if>

```

## Location

Location allows you to check on which URL you are currently accessing the template. This can be useful for activating/deactivating highlighting the navigation. See the following example for usage in your templates:

```
<#if location("ApplicationController:location")>DO SOMETHING</#if>
```

In order to validate the above, you have to pass the Controller and method. Checking is done case-insensitive.

## Route

Route enables you a reverse routing feature. This is useful if you do not want to specify the URLs in your application in a static way and rather want to check if the controller and method do exists. See the following example for usage in your templates:

```
<a href="${route("ApplicationController:route")}">Route</a>
```

The following example will link in the href attribute if the ApplicationController.class route method is mapped to /route

```
<a href="/route">Route</a>
```

You can als pass attributes to the function.

```
<a href="${route("ApplicationController:route", "foo")}">Route</a>
```

The attributes are handled in order and the above example will output the following link

```
<a href="/route/foo">Route</a>
```