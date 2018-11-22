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

package baz.frickl.data;

import com.google.gson.annotations.*;

import java.util.*;

/**
 * @author Sebastian Raubach
 */
public class Image
{
	private String    id;
	private String    name;
	@SerializedName("date_taken")
	private Date      dateTaken;
	private List<Tag> tags;

	public String getId()
	{
		return id;
	}

	public String getName()
	{
		return name;
	}

	public Date getDateTaken()
	{
		return dateTaken;
	}

	public List<Tag> getTags()
	{
		return tags;
	}
}
