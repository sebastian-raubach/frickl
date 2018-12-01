# Frickl

## Background
Flickr [recently announced](https://www.theverge.com/2018/11/1/18051950/flickr-1000-photo-limit-free-accounts-changes-pro-subscription-smugmug) that they're limiting their free plan to 1000 photos. If you're a keen photographer you'll easily exceed this limit quickly. You've got two major options: 1. Pay them for their service or 2. Download all your photos and move away from Flickr.

But what about the hours you spent organising your photos into albums and tagging them?

Fortunately, Flickr allows you to download all your data in one go. Unfortunately, it can take days to get the download link and the data. Once you do get it, the data consists of your images (not the original filename) and lots of `.json` files that contain the metadata.

I spent some time looking for a tool to rename your files back to their original name, but keep the album structure on Flickr and also include the tags in the image file. I couldn't find one, so I wrote one.

## About
**Frickl** ([German(ish) for tinker](https://en.wiktionary.org/wiki/frickeln)) is a Java tool that takes your personal data downloaded from Flickr (all the `data-download-x.zip` and the metadata zip files) and extracts your images in the following manner:

1. Extract image into the album it's part of (this assumes that a photo is only ever on one album or no album at all).
2. Tag (JPG) image with associated keywords by using [IIM4J](https://github.com/vnesek/nmote-iim4j) IPTC metadata.
3. All non-JPG files (videos, PNG, GIF) will not be tagged.

The images will be extracted one by one from the zip files, so no initial extraction of those files is required.

## Requirements

- All your Flickr data (zipped as downloaded) in a folder
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
