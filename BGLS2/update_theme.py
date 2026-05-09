import os
import re

LAYOUT_DIR = r"e:\github\BGLSMOB\BGLS2\app\src\main\res\layout"
JAVA_DIR = r"e:\github\BGLSMOB\BGLS2\app\src\main\java"

# 1. Update item_*.xml files
for filename in os.listdir(LAYOUT_DIR):
    if filename.startswith("item_") and filename.endswith(".xml"):
        filepath = os.path.join(LAYOUT_DIR, filename)
        with open(filepath, 'r', encoding='utf-8') as f:
            content = f.read()
            
        modified = False
        
        # Replace RadioButton with TextView for Action link
        if 'android:id="@+id/rbSelect"' in content and '<RadioButton' in content:
            # We want to replace <RadioButton ... /> with <TextView ... android:text="Action" android:textColor="#007BFF" android:textSize="11sp" />
            # Using regex to find the RadioButton block
            pattern = r'<RadioButton\s+[^>]*android:id="@+id/rbSelect"[^>]*>'
            
            def replace_radio(match):
                block = match.group(0)
                # Change <RadioButton to <TextView
                block = block.replace('<RadioButton', '<TextView')
                # Remove buttonTint
                block = re.sub(r'android:buttonTint="[^"]*"', '', block)
                # Remove scaleX, scaleY
                block = re.sub(r'android:scaleX="[^"]*"', '', block)
                block = re.sub(r'android:scaleY="[^"]*"', '', block)
                # Add text styling if not closed yet
                if '/>' in block:
                    block = block.replace('/>', '\n        android:text="Action"\n        android:textColor="#007BFF"\n        android:textSize="11sp"\n        android:clickable="false"\n        android:focusable="false"/>')
                elif '</RadioButton>' in block:
                    block = block.replace('</RadioButton>', '</TextView>')
                    block = block.replace('>', '\n        android:text="Action"\n        android:textColor="#007BFF"\n        android:textSize="11sp"\n        android:clickable="false"\n        android:focusable="false">')
                return block
                
            new_content = re.sub(pattern, replace_radio, content)
            if new_content != content:
                content = new_content
                modified = True
                
        # Remove background from root layout (assume first tag after xmlns)
        if modified and 'android:background=' in content[:content.find('>', content.find('xmlns:android='))]:
            content = re.sub(r'android:background="[^"]*"', '', content, count=1)
            
        if modified:
            with open(filepath, 'w', encoding='utf-8') as f:
                f.write(content)
            print(f"Updated layout: {filename}")

# 2. Update activity_*.xml files
for filename in os.listdir(LAYOUT_DIR):
    if filename.startswith("activity_") and filename.endswith(".xml"):
        filepath = os.path.join(LAYOUT_DIR, filename)
        with open(filepath, 'r', encoding='utf-8') as f:
            content = f.read()
            
        modified = False
        
        # Change header "Select" to "Action"
        if 'text="Select"' in content and '@style/TableHeader' in content:
            content = content.replace('text="Select"', 'text="Action"')
            modified = True
            
        # Remove grid lines <View android:layout_width="match_parent" android:layout_height="1dp" android:background="#CCCCCC"/>
        if '<View' in content and 'android:background="#CCCCCC"' in content:
            content = re.sub(r'<View[^>]*android:background="#CCCCCC"[^>]*/>\s*', '', content)
            modified = True
            
        if modified:
            with open(filepath, 'w', encoding='utf-8') as f:
                f.write(content)
            print(f"Updated activity: {filename}")

# 3. Update adapters
for root, dirs, files in os.walk(JAVA_DIR):
    for filename in files:
        if filename.endswith("Adapter.kt"):
            filepath = os.path.join(root, filename)
            with open(filepath, 'r', encoding='utf-8') as f:
                content = f.read()
                
            modified = False
            
            # Change RadioButton to TextView
            if 'android.widget.RadioButton' in content:
                content = content.replace('android.widget.RadioButton', 'android.widget.TextView')
                modified = True
                
            if 'val rbSelect: RadioButton' in content:
                content = content.replace('val rbSelect: RadioButton', 'val rbSelect: TextView')
                modified = True
                
            if 'val rbSelect: android.widget.RadioButton' in content:
                content = content.replace('val rbSelect: android.widget.RadioButton', 'val rbSelect: android.widget.TextView')
                modified = True
                
            # Remove isChecked
            if 'holder.rbSelect.isChecked =' in content:
                content = re.sub(r'holder\.rbSelect\.isChecked\s*=\s*[^;\n]*[;\n]', '', content)
                modified = True
                
            # Add Zebra Striping in onBindViewHolder
            if 'override fun onBindViewHolder' in content and 'position % 2 == 0' not in content:
                # Find holder.itemView block
                # We will insert it right after the super call or at the start of the function body
                pattern = r'(override\s+fun\s+onBindViewHolder\s*\([^)]+\)\s*\{)'
                zebra_code = r'\1\n        if (position % 2 == 0) {\n            holder.itemView.setBackgroundColor(android.graphics.Color.parseColor("#FFFFFF"))\n        } else {\n            holder.itemView.setBackgroundColor(android.graphics.Color.parseColor("#F8F9FA"))\n        }\n'
                new_content = re.sub(pattern, zebra_code, content)
                if new_content != content:
                    content = new_content
                    modified = True
                    
            if modified:
                with open(filepath, 'w', encoding='utf-8') as f:
                    f.write(content)
                print(f"Updated adapter: {filename}")
