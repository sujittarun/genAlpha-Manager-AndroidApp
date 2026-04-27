# OpenAI Admission Extraction Setup

This feature runs in a Supabase Edge Function so the OpenAI API key is never stored in the website or Android app.

## One-time setup

1. Install and log in to the Supabase CLI if it is not already configured.
2. From the project root, set your OpenAI API key as a Supabase secret:

```sh
supabase secrets set OPENAI_API_KEY=sk-your-key-here
```

3. Optional: choose the model used for extraction. The default is `gpt-4o-mini`.

```sh
supabase secrets set OPENAI_MODEL=gpt-4o-mini
```

4. Deploy the function:

```sh
supabase functions deploy extract-admission
```

## How it works

The website and Android app upload the scanned/photo admission document to `extract-admission`. The function sends the image or PDF to OpenAI, receives structured JSON, and returns suggested form values for review before submit.

The AI-filled values should always be checked by the parent or staff member before saving the admission.
