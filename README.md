# Frickl
Frickl ([German(ish) for tinker](https://en.wiktionary.org/wiki/frickeln)) is a Java tool that takes your personal data downloaded from Flickr (all the `data-download-x.zip` and the metadata zip files) and extracts your images in the following manner:

1. Extract image into the album it's part of (this assumes that a photo is only ever on one album or no album at all).
2. Tag (JPG) image with associated keywords by using [IIM4J](https://github.com/vnesek/nmote-iim4j) IPTC metadata.
3. All non-JPG files (videos, PNG, GIF) will not be tagged.

The images will be extracted one by one from the zip files, so no initial extraction of those files is required.

## Requirements

- Java 8 or above
- Maven (if you want to build yourself)

## Usage
### Build (optional)
First, build the `frickl.jar` file if it doesn't exist:

```bash
mvn clean compile assembly:single -f pom.xml
```

### Run
```bash
java -jar frickl-X.X.jar <folder containing zip files> <target folder>
```

Make sure that there are no other files in your source folder.
