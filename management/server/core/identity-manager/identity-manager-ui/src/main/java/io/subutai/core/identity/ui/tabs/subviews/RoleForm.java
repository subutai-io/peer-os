package io.subutai.core.identity.ui.tabs.subviews;

import java.util.*;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItem;
import com.vaadin.ui.*;
import io.subutai.core.identity.api.model.Permission;
import io.subutai.core.identity.api.model.Role;
import io.subutai.core.identity.ui.tabs.RolesTab;


public class RoleForm extends VerticalLayout
{

	private Table permTable = new Table ("Permission");
	private List <Permission> perms = new ArrayList<>();
	private BeanItem <Role> currentRole;

	private void addRow (final Permission p)
	{
		Object newItemId = permTable.addItem();
		Item row = permTable.getItem (newItemId);

		row.getItemProperty ("Permission").setValue (String.valueOf (p.getId()));

		ComboBox scopes = new ComboBox();
		scopes.setNullSelectionAllowed(false);
		scopes.setTextInputAllowed(false);
		for (int i = 1; i < 4; ++i)
		{
			scopes.addItem (i);
			switch (i)
			{
				case (1):
				{
					scopes.setItemCaption (1, "All Objects");
					break;
				}
				case (2):
				{
					scopes.setItemCaption (2, "Child Objects");
					break;
				}
				case (3):
				{
					scopes.setItemCaption (3, "Owner Objects");
					break;
				}
			}
		}
		scopes.setValue (p.getScope());
		scopes.addValueChangeListener (new Property.ValueChangeListener()
		{
			@Override
			public void valueChange (Property.ValueChangeEvent event)
			{
				int scope = (int) event.getProperty().getValue();
				p.setScope (scope);
				for (Permission perm : perms)
				{
					if (p.getId() == perm.getId())
					{
						perm.setScope (scope);
					}
				}
			}
		});
		row.getItemProperty("Scope").setValue(scopes);

		CheckBox read = new CheckBox();
		read.setValue (p.isRead());
		read.addValueChangeListener (new Property.ValueChangeListener()
		{
			@Override
			public void valueChange (Property.ValueChangeEvent event)
			{
				boolean v = (boolean) event.getProperty().getValue();
				p.setRead (v);
				for (Permission perm : perms)
				{
					if (p.getId() == perm.getId())
					{
						perm.setRead(v);
					}
				}
			}
		});
		row.getItemProperty("Read").setValue(read);

		CheckBox write = new CheckBox();
		write.setValue (p.isWrite());
		write.addValueChangeListener (new Property.ValueChangeListener()
		{
			@Override
			public void valueChange (Property.ValueChangeEvent event)
			{
				boolean v = (boolean) event.getProperty().getValue();
				p.setWrite(v);
				for (Permission perm : perms)
				{
					if (p.getId() == perm.getId())
					{
						perm.setWrite(v);
					}
				}
			}
		});
		row.getItemProperty ("Write").setValue (write);

		CheckBox update = new CheckBox();
		update.setValue (p.isUpdate());
		update.addValueChangeListener (new Property.ValueChangeListener()
		{
			@Override
			public void valueChange (Property.ValueChangeEvent event)
			{
				boolean v = (boolean) event.getProperty().getValue();
				p.setUpdate(v);
				for (Permission perm : perms)
				{
					if (p.getId() == perm.getId())
					{
						perm.setUpdate(v);
					}
				}
			}
		});
		row.getItemProperty ("Update").setValue (update);

		CheckBox delete = new CheckBox();
		delete.setValue (p.isDelete());
		delete.addValueChangeListener (new Property.ValueChangeListener()
		{
			@Override
			public void valueChange (Property.ValueChangeEvent event)
			{
				boolean v = (boolean) event.getProperty().getValue();
				p.setDelete (v);
				for (Permission perm : perms)
				{
					if (p.getId() == perm.getId())
					{
						perm.setDelete(v);
					}
				}
			}
		});
		row.getItemProperty ("Delete").setValue (delete);
	}

	public void setRole (BeanItem <Role> role)
	{
		this.currentRole = role;
		this.perms.clear();
		for (Permission p : currentRole.getBean().getPermissions())
		{
			this.perms.add (p);
			this.addRow(p);
		}
	}

	public RoleForm (final RolesTab callback)
	{
		this.setSpacing (true);
		permTable.addContainerProperty ("Permission", String.class, null);
		permTable.addContainerProperty ("Scope", ComboBox.class, null);
		permTable.addContainerProperty ("Read", CheckBox.class, null);
		permTable.addContainerProperty ("Write", CheckBox.class, null);
		permTable.addContainerProperty ("Update", CheckBox.class, null);
		permTable.addContainerProperty ("Delete", CheckBox.class, null);
		Button close = new Button ("Close");
		close.addClickListener (new Button.ClickListener()
		{
			@Override
			public void buttonClick (Button.ClickEvent event)
			{
				callback.cancelOperation();
			}
		});
		Button save = new Button ("Save");
		save.addClickListener(new Button.ClickListener()
		{
			@Override
			public void buttonClick(Button.ClickEvent clickEvent)
			{
				currentRole.getBean().setPermissions (perms);
				callback.saveOperation(currentRole, false);
			}
		});
		HorizontalLayout buttonGrid = new HorizontalLayout();
		buttonGrid.setSpacing (true);
		buttonGrid.addComponent (close);
		buttonGrid.addComponent (save);
		this.addComponent (permTable);
		this.addComponent (buttonGrid);
	}

}
