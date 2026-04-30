import re

with open('/Users/sayashri/Documents/BGLSMOB/BGLS2/app/src/main/res/layout/activity_loan_master_view.xml', 'r') as f:
    content = f.read()

def to_camel_case(s):
    parts = re.split(r'[^a-zA-Z0-9]', s)
    return 'et' + ''.join(p.capitalize() for p in parts if p)

# Find all blocks like <TextView ... text="Some Label" ... <EditText ... />
# and inject android:id="@+id/etSomeLabel" into the EditText if it doesn't have an ID
pattern = re.compile(r'<TextView[^>]*?android:text="([^"]+)"[^>]*?>\s*<EditText\s+([^>]+)/>', re.DOTALL)

def repl(m):
    label = m.group(1)
    et_attrs = m.group(2)
    if 'android:id' not in et_attrs:
        id_str = to_camel_case(label)
        et_attrs = f'android:id="@+id/{id_str}" ' + et_attrs
    
    # We also need to strip out android:text="..." from EditText so it's empty by default
    et_attrs = re.sub(r'android:text="[^"]*"', 'android:text=""', et_attrs)
    
    # Reconstruct the matched string
    return m.group(0).replace(m.group(2), et_attrs)

new_content = pattern.sub(repl, content)

with open('/Users/sayashri/Documents/BGLSMOB/BGLS2/app/src/main/res/layout/activity_loan_master_view.xml', 'w') as f:
    f.write(new_content)

print("Done updating IDs")
