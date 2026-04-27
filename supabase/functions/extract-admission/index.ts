const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "authorization, x-client-info, apikey, content-type",
  "Access-Control-Allow-Methods": "POST, OPTIONS",
};

const extractionSchema = {
  type: "object",
  additionalProperties: false,
  required: ["fields", "confidence", "warnings"],
  properties: {
    confidence: { type: "number" },
    warnings: {
      type: "array",
      items: { type: "string" },
    },
    fields: {
      type: "object",
      additionalProperties: false,
      required: [
        "applicantName",
        "nationality",
        "dateOfBirth",
        "age",
        "gender",
        "fatherGuardianName",
        "alternateContactNo",
        "parentContactNo",
        "city",
        "address",
        "schoolCollege",
        "parentAadhaarNo",
        "timeSlot",
        "batsmanStyle",
        "bowlingStyles",
        "readyToStartNow",
        "comments",
      ],
      properties: {
        applicantName: { type: "string" },
        nationality: { type: "string" },
        dateOfBirth: { type: "string" },
        age: { type: "integer" },
        gender: { type: "string" },
        fatherGuardianName: { type: "string" },
        alternateContactNo: { type: "string" },
        parentContactNo: { type: "string" },
        city: { type: "string" },
        address: { type: "string" },
        schoolCollege: { type: "string" },
        parentAadhaarNo: { type: "string" },
        timeSlot: { type: "string" },
        batsmanStyle: { type: "string" },
        bowlingStyles: {
          type: "array",
          items: { type: "string" },
        },
        readyToStartNow: { type: "boolean" },
        comments: { type: "string" },
      },
    },
  },
};

function jsonResponse(body: unknown, status = 200) {
  return new Response(JSON.stringify(body), {
    status,
    headers: {
      ...corsHeaders,
      "Content-Type": "application/json",
    },
  });
}

function findOutputText(payload: any): string {
  if (typeof payload?.output_text === "string") {
    return payload.output_text;
  }

  for (const item of payload?.output ?? []) {
    for (const content of item?.content ?? []) {
      if (content?.type === "output_text" && typeof content?.text === "string") {
        return content.text;
      }
    }
  }

  return "";
}

Deno.serve(async (request) => {
  if (request.method === "OPTIONS") {
    return new Response("ok", { headers: corsHeaders });
  }

  if (request.method !== "POST") {
    return jsonResponse({ error: "Method not allowed" }, 405);
  }

  const apiKey = Deno.env.get("OPENAI_API_KEY");
  if (!apiKey) {
    return jsonResponse({ error: "OPENAI_API_KEY is not configured in Supabase secrets." }, 500);
  }

  try {
    const { fileBase64, imageBase64, mimeType, fileName } = await request.json();
    const encodedFile = String(fileBase64 || imageBase64 || "").replace(/^data:[^;]+;base64,/, "");
    const contentType = String(mimeType || "image/jpeg");

    if (!encodedFile) {
      return jsonResponse({ error: "Upload an admission form image or PDF first." }, 400);
    }

    const isPdf = contentType.includes("pdf") || String(fileName || "").toLowerCase().endsWith(".pdf");
    const filePart = isPdf
      ? {
          type: "input_file",
          filename: fileName || "admission-form.pdf",
          file_data: `data:${contentType};base64,${encodedFile}`,
        }
      : {
          type: "input_image",
          image_url: `data:${contentType};base64,${encodedFile}`,
          detail: "high",
        };

    const openAiResponse = await fetch("https://api.openai.com/v1/responses", {
      method: "POST",
      headers: {
        Authorization: `Bearer ${apiKey}`,
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        model: Deno.env.get("OPENAI_MODEL") || "gpt-4o-mini",
        input: [
          {
            role: "user",
            content: [
              {
                type: "input_text",
                text:
                  "Extract handwritten and printed details from this Gen Alpha Cricket Academy admission form into JSON. " +
                  "Fill only values you can read with reasonable confidence. Use empty strings for unclear or missing fields. " +
                  "Return dateOfBirth as YYYY-MM-DD only when a full date is visible; otherwise empty string. " +
                  "If only age/class is visible, put age only if it is clearly the applicant age; put class/grade in comments. " +
                  "Normalize gender to Male or Female when possible. Normalize contacts to digits only. " +
                  "Map emergency contact to alternateContactNo. Map Studying in Class to comments. " +
                  "Use timeSlot only if it matches 6AM, 7:30AM, 4PM, 5:30PM, or 7PM.",
              },
              filePart,
            ],
          },
        ],
        text: {
          format: {
            type: "json_schema",
            name: "admission_extraction",
            strict: true,
            schema: extractionSchema,
          },
        },
      }),
    });

    const responseBody = await openAiResponse.json();
    if (!openAiResponse.ok) {
      return jsonResponse(
        { error: responseBody?.error?.message || "OpenAI could not read this document." },
        openAiResponse.status,
      );
    }

    const outputText = findOutputText(responseBody);
    if (!outputText) {
      return jsonResponse({ error: "OpenAI returned an empty extraction result." }, 502);
    }

    return jsonResponse(JSON.parse(outputText));
  } catch (error) {
    return jsonResponse({ error: error instanceof Error ? error.message : "Unable to extract admission details." }, 500);
  }
});
