# Admission Document Extraction Setup

This feature runs in a Supabase Edge Function so AI provider keys are never stored in the website or Android app.

The function uses Azure Document Intelligence first when Azure secrets are configured. OpenAI remains available as a future fallback if you later add API credits.

## Azure free option

Azure Document Intelligence has a free F0 tier with 500 pages per month. This is the best free fit for the academy's current upload volume.

Create an Azure AI Document Intelligence resource, choose the free `F0` pricing tier, then copy:

- Endpoint
- Key 1

Add those as Supabase secrets:

```sh
supabase secrets set AZURE_DOCUMENT_INTELLIGENCE_ENDPOINT=https://your-resource.cognitiveservices.azure.com/
supabase secrets set AZURE_DOCUMENT_INTELLIGENCE_KEY=your-azure-key
```

Deploy the function without JWT verification:

```sh
supabase functions deploy extract-admission --no-verify-jwt
```

## Optional OpenAI setup

1. Install and log in to the Supabase CLI if it is not already configured.
2. From the project root, set your OpenAI API key as a Supabase secret:

```sh
supabase secrets set OPENAI_API_KEY=sk-your-key-here
```

3. Optional: choose the model used for extraction. The default is `gpt-4o-mini`.

```sh
supabase secrets set OPENAI_MODEL=gpt-4o-mini
```

4. Deploy the function without JWT verification. This is required because the public website and Android app use the Supabase publishable key, not a user login JWT.

```sh
supabase functions deploy extract-admission --no-verify-jwt
```

## How it works

The website and Android app upload the scanned/photo admission document to `extract-admission`. The function sends the image or PDF to OpenAI, receives structured JSON, and returns suggested form values for review before submit.

The AI-filled values should always be checked by the parent or staff member before saving the admission.
