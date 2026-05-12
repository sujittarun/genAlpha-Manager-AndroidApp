import re
details = "Parent replied with image. Proof stored at payment-proofs/2a9ea5f7-4844-4263-922c-cfbb1d66420a/wamidHBgMOTE5OTUxNTk3NTY3FQIAEhggQUNFQTU1RERFNDY0QTk4NDNCRjk3MzBEQzYzRDRGQzUA.jpg."
pattern = r"payment-proofs/([^\s.]+/[^\s.]+\.(?:jpg|jpeg|png|webp|pdf))"
match = re.search(pattern, details, re.IGNORECASE)
print(match.group(1))
