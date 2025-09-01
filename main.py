"""
Main interface for the shopping cart system.
"""

from store import Store

POSSIBLE_ACTIONS = [
    'search_by_name',
    'add_item',
    'remove_item',
    'show_cart',
    'show_store',
    'checkout',
    'exit'
]

ITEMS_FILE = 'items.yml'


def read_input():
    """Get user input and parse action with parameters."""
    line = input('\nWhat would you like to do? ').strip()
    if not line:
        return '', ''
    
    parts = line.split(' ', 1)
    action = parts[0]
    params = parts[1] if len(parts) > 1 else ''
    return action, params


def print_search_results(items):
    """Print search results in a nice format."""
    if not items:
        print("No items found.")
        return
    
    print(f"\nFound {len(items)} items:")
    print("-" * 50)
    for i, item in enumerate(items, 1):
        print(f"{i}. {item.name} - ${item.price:.2f}")
        print(f"   Stock: {item.stock} units")
        print()


def handle_add_item(store, params):
    """Handle adding items with optional quantity."""
    if not params:
        print("Please specify an item name.")
        return
    
    parts = params.rsplit(' ', 1)  # Split from right to get quantity
    item_name = parts[0]
    quantity = 1  # default
    
    # Check if last part is a number (quantity)
    if len(parts) == 2:
        try:
            quantity = int(parts[1])
            if quantity <= 0:
                print("Quantity must be positive.")
                return
        except ValueError:
            # Not a number, treat as part of item name
            item_name = params
            quantity = 1
    
    result = store.add_item(item_name, quantity)
    print(result['message'])


def handle_remove_item(store, params):
    """Handle removing items with optional quantity."""
    if not params:
        print("Please specify an item name.")
        return
    
    parts = params.rsplit(' ', 1)
    item_name = parts[0]
    quantity = None  # default (remove all)
    
    # Check if last part is a number (quantity)
    if len(parts) == 2:
        try:
            quantity = int(parts[1])
            if quantity <= 0:
                print("Quantity must be positive.")
                return
        except ValueError:
            # Not a number, treat as part of item name
            item_name = params
            quantity = None
    
    result = store.remove_item(item_name, quantity)
    print(result['message'])


def main():
    """Main program loop."""
    print("Welcome to the Online Store!")
    print("=" * 40)
    print("Available actions:")
    print("• search_by_name <term>")
    print("• add_item <name> [quantity]")
    print("• remove_item <name> [quantity]") 
    print("• show_cart")
    print("• show_store")
    print("• checkout")
    print("• exit")
    
    try:
        store = Store(ITEMS_FILE)
    except FileNotFoundError:
        print(f"Error: Could not find {ITEMS_FILE}")
        return
    except Exception as e:
        print(f"Error loading store: {e}")
        return
    
    while True:
        action, params = read_input()
        
        if action == 'exit':
            print("Thank you for shopping with us!")
            break
        
        if action not in POSSIBLE_ACTIONS:
            print("Invalid action. Try: search_by_name, add_item, remove_item, show_cart, checkout, exit")
            continue
        
        try:
            if action == 'search_by_name':
                if not params:
                    print("Please provide a search term.")
                    continue
                results = store.search_by_name(params)
                print_search_results(results)
            
            elif action == 'add_item':
                handle_add_item(store, params)
            
            elif action == 'remove_item':
                handle_remove_item(store, params)
            
            elif action == 'show_cart':
                store.show_cart()
            elif action == 'show_store':
                print(store)        
            elif action == 'checkout':
                result = store.checkout()
                print(result['message'])
                if result['success']:
                    break
        
        except Exception as e:
            print(f"An error occurred: {e}")


if __name__ == '__main__':
    main()