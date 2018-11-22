/*
 * Copyright 2018 Sebastian Raubach <sebastian@raubach.co.uk>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package baz.frickl.cmd.io;

import com.google.gson.*;
import com.nmote.iim4j.*;
import com.nmote.iim4j.dataset.*;
import com.nmote.iim4j.stream.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.zip.*;

import baz.frickl.data.*;

/**
 * @author Sebastian Raubach
 */
public class ZipExtractor
{
	/** Stores the Flickr ID to album name mapping */
	private Map<String, String> imageToAlbum;
	/** Stores the Flickr ID to {@link Image} mapping */
	private Map<String, Image>  images;

	public void run(File input, File output)
		throws IOException
	{
		imageToAlbum = new HashMap<>();
		images = new HashMap<>();

		// Get only the config zips first
		File[] config = input.listFiles(f -> f.getName().endsWith(".zip") && !f.getName().startsWith("data-download"));

		if (config != null)
		{
			for (File file : config)
			{
				ZipFile zip = new ZipFile(file);
				Enumeration<? extends ZipEntry> enumeration = zip.entries();

				ZipEntry entry;
				while (enumeration.hasMoreElements())
				{
					entry = enumeration.nextElement();
					// Read and store the albums
					if (entry.getName().equals("albums.json"))
					{
						parseAlbum(zip, entry);
					}
					// Then parse the individual image files for the tags
					else if (entry.getName().startsWith("photo_"))
					{
						parseImage(zip, entry);
					}
				}
			}

			// Now actually look at the images
			File[] imageFiles = input.listFiles(f -> f.getName().startsWith("data-download"));

			if (imageFiles != null)
			{
				for (File file : imageFiles)
				{
					ZipFile zip = new ZipFile(file);
					Enumeration<? extends ZipEntry> enumeration = zip.entries();

					ZipEntry entry;
					while (enumeration.hasMoreElements())
					{
						entry = enumeration.nextElement();

						String name = entry.getName();
						FileType type = FileType.getFromName(name);
						// Get the extension
						String ext = type.extension != null ? type.extension : name.substring(name.lastIndexOf(".") + 1);
						// Determine of we need to split off the "_o" part (images have it, other files don't)
						String replace = type.isImage ? "_o." + ext : "." + ext;
						// Extract the Flickr ID
						name = name.replace(replace, "");
						name = name.substring(name.lastIndexOf("_") + 1);

						// Get the image information and the album
						Image image = images.get(name);
						String album = imageToAlbum.get(name);

						// Create a fallback album
						if (album == null || "".equals(album))
							album = "UNKNOWN";

						File folder = new File(output, album);
						folder.mkdirs();
						// This is the target file
						File target = new File(folder, image.getName() + "." + ext);

						// Stream it out
						try (InputStream is = zip.getInputStream(entry);
							 FileOutputStream fos = new FileOutputStream(target))
						{
							byte[] bytes = new byte[1024];
							int length;
							while ((length = is.read(bytes)) >= 0)
							{
								fos.write(bytes, 0, length);
							}
						}

						// Now tag, if it's a JPG
						if (type == FileType.JPEG || type == FileType.JPG)
							tagImage(image, target);
						else
							System.out.println(target.getAbsolutePath());
					}
				}
			}
		}
	}

	/**
	 * Tags the given {@link File} with the {@link Tag} information in the {@link Image} instance
	 *
	 * @param image The {@link Image} containing the {@link Tag}s
	 * @param file  The {@link File} representing the image
	 */
	private void tagImage(Image image, File file)
	{
		if (image.getTags() != null && image.getTags().size() > 0)
		{
			try
			{
				IIMFile iimFile = new IIMFile();
				for (Tag tag : image.getTags())
				{
					iimFile.add(IIM.KEYWORDS, tag.getTag());
				}
				iimFile.validate();

				File target = new File(file.getParent(), "temp_" + file.getName());
				try (InputStream in = new FileInputStream(file);
					 OutputStream out = new FileOutputStream(target))
				{
					JPEGUtil.insertIIMIntoJPEG(out, iimFile, in);
				}
				catch (Exception e)
				{
					System.err.println("Corrupt image: " + file.getAbsolutePath());
					e.printStackTrace();
				}

				Files.move(target.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
			}
			catch (InvalidDataSetException | IOException e)
			{
				e.printStackTrace();
			}
		}
	}

	/**
	 * Parse the {@link ZipEntry} and store the result in {@link #images}.
	 *
	 * @param z The {@link ZipFile} that's the parent of {@link ZipEntry}.
	 * @param f The {@link ZipEntry} representing the file.
	 * @throws IOException Thrown if the file interaction fails.
	 */
	private void parseImage(ZipFile z, ZipEntry f)
		throws IOException
	{
		Gson gson = new GsonBuilder()
			.setDateFormat("yyyy-MM-dd HH:mm:ss").create();
		Image image = gson.fromJson(new InputStreamReader(z.getInputStream(f)), Image.class);

		images.put(image.getId(), image);
	}

	/**
	 * Parses the {@link ZipEntry} and stores the {@link Album} information in {@link #imageToAlbum}.
	 *
	 * @param z The {@link ZipFile} that's the parent of {@link ZipEntry}.
	 * @param f The {@link ZipEntry} representing the file.
	 * @throws IOException Thrown if the file interaction fails.
	 */
	private void parseAlbum(ZipFile z, ZipEntry f)
		throws IOException
	{
		Gson gson = new Gson();
		Albums albums = gson.fromJson(new InputStreamReader(z.getInputStream(f)), Albums.class);

		for (Album album : albums.getAlbums())
		{
			for (String photo : album.getPhotos())
			{
				if (photo.equals("0"))
					continue;
				imageToAlbum.put(photo, album.getTitle());
			}
		}
	}

	private enum FileType
	{
		JPG(true, "jpg"),
		JPEG(true, "jpeg"),
		GIF(true, "gif"),
		PNG(true, "png"),
		OTHER(false);

		private boolean isImage;
		private String  extension = null;

		FileType(boolean isImage)
		{
			this.isImage = isImage;
		}

		FileType(boolean isImage, String extension)
		{
			this.isImage = isImage;
			this.extension = extension;
		}

		public static FileType getFromName(String filename)
		{
			filename = filename.toLowerCase();

			for (FileType type : values())
			{
				if (type.extension != null)
				{
					if (filename.endsWith("." + type.extension))
						return type;
				}
			}

			return OTHER;
		}
	}
}
