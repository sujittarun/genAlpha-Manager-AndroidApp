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

function emptyFields() {
  return {
    applicantName: "",
    nationality: "",
    dateOfBirth: "",
    age: 0,
    gender: "",
    fatherGuardianName: "",
    alternateContactNo: "",
    parentContactNo: "",
    city: "",
    address: "",
    schoolCollege: "",
    parentAadhaarNo: "",
    timeSlot: "",
    batsmanStyle: "",
    bowlingStyles: [] as string[],
    readyToStartNow: false,
    comments: "",
  };
}

function normalizeWhitespace(value: string): string {
  return value.replace(/\s+/g, " ").trim();
}

function normalizePhone(value: string): string {
  return value.replace(/\D/g, "").slice(-10);
}

function extractAfterLabel(text: string, labels: string[], stopLabels: string[] = []): string {
  const escapedLabels = labels.map((label) => label.replace(/[.*+?^${}()|[\]\\]/g, "\\$&")).join("|");
  const escapedStops = stopLabels.map((label) => label.replace(/[.*+?^${}()|[\]\\]/g, "\\$&")).join("|");
  const stopPattern = escapedStops ? `(?=\\s+(?:${escapedStops})\\b|$)` : "(?=$)";
  const pattern = new RegExp(`(?:${escapedLabels})\\s*[:\\-]?\\s*(.*?)${stopPattern}`, "i");
  return normalizeWhitespace(pattern.exec(text)?.[1] || "");
}

function parseAzureAdmissionText(rawText: string) {
  const text = normalizeWhitespace(rawText);
  const fields = emptyFields();
  const warnings: string[] = [];

  fields.applicantName = extractAfterLabel(text, ["Applicant's Full Name", "Applicant Full Name", "Applicant Name"], [
    "Date of Birth",
    "Gender",
    "Nationality",
  ]);
  fields.nationality = extractAfterLabel(text, ["Nationality"], ["Father", "Emergency", "Date of Birth"]) || "Indian";
  fields.gender = extractAfterLabel(text, ["Gender"], ["Nationality", "Father", "Emergency"]).replace(/^m$/i, "Male").replace(/^f$/i, "Female");
  fields.fatherGuardianName = extractAfterLabel(text, ["Father's / Guardian's Name", "Father's Guardian's Name", "Guardian's Name"], [
    "Parent's Contact",
    "Emergency Contact",
    "Alternate Contact",
  ]);
  fields.address = extractAfterLabel(text, ["Address"], ["City", "School", "Studying", "Parent"]);
  fields.city = extractAfterLabel(text, ["City"], ["Studying", "School", "Parent"]);
  fields.schoolCollege = extractAfterLabel(text, ["School / College", "School College", "School"], ["Studying", "Parent", "NIDA"]);
  fields.parentAadhaarNo = extractAfterLabel(text, ["Parent's Aadhaar No", "Parent's Aadhar No", "Aadhaar No", "Aadhar No"], ["Skills", "Batsman"]);
  fields.timeSlot = normalizeTimeSlot(extractAfterLabel(text, ["Preferred slot time", "Choose time slot"], ["Reg No", "Applicant"]));

  const parentContact = extractAfterLabel(text, ["Parent's Contact No", "Parent Contact No"], [
    "Emergency Contact",
    "Alternate Contact",
    "Address",
  ]);
  const alternateContact = extractAfterLabel(text, ["Emergency Contact No", "Alternate Contact No"], ["Address", "City", "School"]);
  fields.parentContactNo = normalizePhone(parentContact);
  fields.alternateContactNo = normalizePhone(alternateContact);

  const ageText = extractAfterLabel(text, ["Date of Birth / Age", "DOB / Age", "Age"], ["Gender", "Nationality", "Father"]);
  const ageMatch = /\b([4-9]|1[0-8])\b/.exec(ageText);
  fields.age = ageMatch ? Number(ageMatch[1]) : 0;

  const dobMatch = /\b(\d{1,2})[\/\-.](\d{1,2})[\/\-.](\d{2,4})\b/.exec(ageText);
  if (dobMatch) {
    const year = dobMatch[3].length === 2 ? `20${dobMatch[3]}` : dobMatch[3];
    fields.dateOfBirth = `${year}-${dobMatch[2].padStart(2, "0")}-${dobMatch[1].padStart(2, "0")}`;
  }

  const grade = extractAfterLabel(text, ["Studying in Class", "Class"], ["Parent", "NIDA", "Skills"]);
  if (grade) {
    fields.comments = `Studying in class: ${grade}`;
  }

  if (/kick start/i.test(text)) {
    fields.readyToStartNow = true;
  }
  if (/right handed batsman/i.test(text)) {
    fields.batsmanStyle = "Right Handed Batsman";
  } else if (/left handed batsman/i.test(text)) {
    fields.batsmanStyle = "Left Handed Batsman";
  }

  fields.bowlingStyles = [
    "Right Arm Fast Bowler",
    "Left Arm Fast Bowler",
    "Right Arm Off Spinner",
    "Left Arm Leg Spinner",
  ].filter((style) => new RegExp(style.replace(/\s+/g, "\\s+"), "i").test(text));

  if (!fields.applicantName) warnings.push("Applicant name was not clear.");
  if (!fields.parentContactNo) warnings.push("Parent contact number was not clear.");
  if (!fields.dateOfBirth && !fields.age) warnings.push("Date of birth was not clear.");

  return {
    fields,
    confidence: warnings.length > 0 ? 0.58 : 0.72,
    warnings,
  };
}

function normalizeTimeSlot(value: string): string {
  const compact = value.toUpperCase().replace(/\s+/g, "");
  return ["6AM", "7:30AM", "4PM", "5:30PM", "7PM"].find((slot) => slot.toUpperCase().replace(/\s+/g, "") === compact) || "";
}

function base64ToBytes(base64: string): Uint8Array {
  const binary = atob(base64);
  const bytes = new Uint8Array(binary.length);
  for (let index = 0; index < binary.length; index += 1) {
    bytes[index] = binary.charCodeAt(index);
  }
  return bytes;
}

async function extractWithAzure(encodedFile: string, contentType: string) {
  const endpoint = Deno.env.get("AZURE_DOCUMENT_INTELLIGENCE_ENDPOINT")?.replace(/\/+$/, "");
  const key = Deno.env.get("AZURE_DOCUMENT_INTELLIGENCE_KEY");
  if (!endpoint || !key) {
    return null;
  }

  const analyzeResponse = await fetch(
    `${endpoint}/documentintelligence/documentModels/prebuilt-layout:analyze?api-version=2024-11-30`,
    {
      method: "POST",
      headers: {
        "Ocp-Apim-Subscription-Key": key,
        "Content-Type": contentType,
      },
      body: base64ToBytes(encodedFile),
    },
  );

  if (!analyzeResponse.ok) {
    const body = await analyzeResponse.text();
    throw new Error(`Azure Document Intelligence failed: ${body || analyzeResponse.statusText}`);
  }

  const operationLocation = analyzeResponse.headers.get("operation-location");
  if (!operationLocation) {
    throw new Error("Azure Document Intelligence did not return an operation location.");
  }

  for (let attempt = 0; attempt < 18; attempt += 1) {
    await new Promise((resolve) => setTimeout(resolve, 1000));
    const resultResponse = await fetch(operationLocation, {
      headers: { "Ocp-Apim-Subscription-Key": key },
    });
    const result = await resultResponse.json();

    if (result.status === "succeeded") {
      const content = result.analyzeResult?.content || "";
      return parseAzureAdmissionText(content);
    }
    if (result.status === "failed") {
      throw new Error(result.error?.message || "Azure could not read this document.");
    }
  }

  throw new Error("Azure document reading timed out. Try a clearer photo or smaller file.");
}

Deno.serve(async (request) => {
  if (request.method === "OPTIONS") {
    return new Response("ok", { headers: corsHeaders });
  }

  if (request.method !== "POST") {
    return jsonResponse({ error: "Method not allowed" }, 405);
  }

  try {
    const { fileBase64, imageBase64, mimeType, fileName } = await request.json();
    const encodedFile = String(fileBase64 || imageBase64 || "").replace(/^data:[^;]+;base64,/, "");
    const contentType = String(mimeType || "image/jpeg");

    if (!encodedFile) {
      return jsonResponse({ error: "Upload an admission form image or PDF first." }, 400);
    }

    const azureResult = await extractWithAzure(encodedFile, contentType);
    if (azureResult) {
      return jsonResponse(azureResult);
    }

    const apiKey = Deno.env.get("OPENAI_API_KEY");
    if (!apiKey) {
      return jsonResponse({
        error:
          "No AI provider is configured. Add Azure Document Intelligence secrets or OpenAI API credits.",
      }, 500);
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
