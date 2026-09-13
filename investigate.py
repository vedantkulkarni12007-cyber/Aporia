import urllib.request
import json

def fetch_wikidata(qid):
    req = urllib.request.Request(
        f'https://www.wikidata.org/w/api.php?action=wbgetentities&ids={qid}&languages=en&props=labels|descriptions|claims&format=json',
        headers={'User-Agent': 'Aporia/1.0 (test@example.com)'}
    )
    res = json.loads(urllib.request.urlopen(req).read())
    claims = res['entities'][qid]['claims']
    
    print(f"--- {qid} ---")
    for p in claims:
        # fetch property label
        try:
            preq = urllib.request.Request(
                f'https://www.wikidata.org/w/api.php?action=wbgetentities&ids={p}&languages=en&props=labels&format=json',
                headers={'User-Agent': 'Aporia/1.0 (test@example.com)'}
            )
            pres = json.loads(urllib.request.urlopen(preq).read())
            plabel = pres['entities'][p]['labels']['en']['value']
        except:
            plabel = p
            
        print(f'{p} ({plabel}): {len(claims[p])} claims')
        for claim in claims[p][:5]:
            try:
                target_id = claim['mainsnak']['datavalue']['value']['id']
                # fetch target label
                treq = urllib.request.Request(
                    f'https://www.wikidata.org/w/api.php?action=wbgetentities&ids={target_id}&languages=en&props=labels&format=json',
                    headers={'User-Agent': 'Aporia/1.0 (test@example.com)'}
                )
                tres = json.loads(urllib.request.urlopen(treq).read())
                tlabel = tres['entities'][target_id]['labels']['en']['value']
                print(f'  -> {target_id} ({tlabel})')
            except:
                pass

fetch_wikidata('Q336') # Science
fetch_wikidata('Q11436') # Astronomy
