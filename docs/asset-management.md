mangoo I/O offers build-in functionalities for handling asset management as well as options for pre-processing SASS and LESS Files.

## On the fly asset minification

When in dev mode, mangoo I/O offers you the ability to minify CSS and JS resources on-the-fly, giving a front-end developer the opportunity to work in the raw CSS and JS files and have the minified version linked in the default template of your application. Thus, there is no need for extra minification or post processing before deployment to a production environment.

By default minification of CSS and JS resources is disabled and has to be enable with the following options

```properties
[application]
	minify.js = true
	minify.css = true
```

By convention, if on-the-fly minification is activated mangoo I/O will check for changes in all files ending with .css or .js that have no “min” in their file name and are located in the following folder

```properties
/src/main/resources/files/assets
```

Once a file is changed, mangoo I/O will automatically minify the file. Already minified files, for example jquery.min.js will not be minified again. The on-the-fly minification will create a file with the same name, ending with .min.css or .min.js.

Of course you can configure the folder for the CSS and JS files in your application. See Configuration options for more information about this.

## Maven goals

Asset minification and preprocessing of SASS/LESS files can not only be done on-the-fly in developement mode, but also via Maven goals, e.g. in your build process.

The following command minifies CSS and JS files

```bash
mvn mangooio:minify
```

Please note, that all minified CSS files will, by convention, result in a file with the same name as the CSS file and a .min.css suffix in the following folder

```bash
/src/main/resources/files/assets/stylesheet
```

Minified JS files will will, by conecntion, result in a file with the same name as the JS file and a .min.js suffice in the following folder

```bash
/src/main/resources/files/assets/javascript
```