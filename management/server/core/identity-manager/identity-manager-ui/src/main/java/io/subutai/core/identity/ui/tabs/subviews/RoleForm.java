package io.subutai.core.identity.ui.tabs.subviews;

import java.util.*;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItem;
import com.vaadin.server.Page;
import com.vaadin.ui.*;

import io.subutai.common.security.objects.PermissionScope;
import io.subutai.core.identity.api.model.Permission;
import io.subutai.core.identity.api.model.Role;
import io.subutai.core.identity.ui.tabs.RolesTab;
import io.subutai.common.security.objects.PermissionObject;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

public class RoleForm extends Window
{

	private Table permTable = new Table ("Assigned permissions");
	private Table allPerms = new Table ("All permissions");
	private List <Permission> perms = new ArrayList<>();
	private BeanItem <Role> currentRole;

	private void addRow (final Permission p)
	{
		Object newItemId = permTable.addItem();
		Item row = permTable.getItem (newItemId);

		row.getItemProperty ("Permission").setValue (p.getObjectName());

		ComboBox scopes = new ComboBox();
		scopes.setNullSelectionAllowed(false);
		scopes.setTextInputAllowed(false);

        for (int i = 0; i < PermissionScope.values().length; ++i)
        {
            scopes.addItem(i+1);
            scopes.setItemCaption( i + 1, PermissionScope.values()[i].getName() );
        }

		scopes.setValue (p.getScope());
		scopes.addValueChangeListener (new Property.ValueChangeListener()
		{
			@Override
			public void valueChange (Property.ValueChangeEvent event)
			{
				int scope = (int) event.getProperty().getValue();
				p.setScope (scope);

				/*
                for (Permission perm : perms)
				{
					if (p.getId() == perm.getId())
					{
						perm.setScope (scope);
					}
				}*/
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

                /*
				for (Permission perm : perms)
				{
					if (p.getId() == perm.getId())
					{
						perm.setRead(v);
					}
				}*/
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

                /*
                for (Permission perm : perms)
				{
					if (p.getId() == perm.getId())
					{
						perm.setWrite(v);
					}
				}*/
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
				/*
                p.setUpdate(v);
				for (Permission perm : perms)
				{
					if (p.getId() == perm.getId())
					{
						perm.setUpdate(v);
					}
				}*/
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
				/*
                boolean v = (boolean) event.getProperty().getValue();
				p.setDelete (v);
				for (Permission perm : perms)
				{
					if (p.getId() == perm.getId())
					{
						perm.setDelete(v);
					}
				}*/
			}
		});
		row.getItemProperty ("Delete").setValue (delete);


		final Button remove = new Button ("x");
        remove.setWidth( "32px" );
        remove.setData (newItemId);
        remove.addClickListener (new Button.ClickListener()
		{
			@Override
			public void buttonClick (Button.ClickEvent event)
			{
				/*
                for (int i = 0; i < perms.size(); ++i)
				{
					if (perms.get (i).getId() == p.getId())
					{
						perms.remove (i);
						break;
					}
				}*/
				permTable.removeItem (remove.getData());
			}
		});
		row.getItemProperty ("Remove").setValue (remove);
	}


	private void addPerms (RolesTab callback)
	{
		for (int a=0; a< PermissionObject.values().length;a++)
		{
            final Object newItemId = allPerms.addItem();
			final Item row = allPerms.getItem (newItemId);

			row.getItemProperty ("Permission").setValue (PermissionObject.values()[a].getName());

			Button add = new Button ("Add");
			add.addClickListener (new Button.ClickListener()
			{
				@Override
				public void buttonClick (Button.ClickEvent event)
				{

                }
			});
			row.getItemProperty ("Add").setValue( add );
		}
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
		this.setClosable (false);
        this.addStyleName ("default");
		this.center();

        VerticalLayout content = new VerticalLayout();
		content.setSpacing (true);
		content.setMargin (true);
		permTable.addContainerProperty ("Permission", String.class, null);
		permTable.addContainerProperty ("Scope", ComboBox.class, null);
		permTable.addContainerProperty ("Read", CheckBox.class, null);
		permTable.addContainerProperty ("Write", CheckBox.class, null);
		permTable.addContainerProperty ("Update", CheckBox.class, null);
		permTable.addContainerProperty ("Delete", CheckBox.class, null);
		permTable.addContainerProperty ("Remove", Button.class, null);


        permTable.setColumnWidth("Scope",114  );
        permTable.setColumnWidth("Read",34  );
        permTable.setColumnWidth("Write",46  );
        permTable.setColumnWidth("Update",49  );
        permTable.setColumnWidth("Delete",46  );
        permTable.setColumnWidth("Remove",62  );
        permTable.setHeight( "250px" );


		allPerms.addContainerProperty("Permission", String.class, null);
		allPerms.addContainerProperty ("Add", Button.class, null);
        allPerms.setHeight( "250px" );

		addPerms (callback);
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
			public void buttonClick (Button.ClickEvent clickEvent)
			{
				currentRole.getBean().setPermissions (perms);
				callback.saveOperation(currentRole, false);
			}
		});
		HorizontalLayout buttonGrid = new HorizontalLayout();
		buttonGrid.setSpacing (true);
		buttonGrid.addComponent(close);
		buttonGrid.addComponent(save);
		HorizontalLayout tableGrid = new HorizontalLayout();
        tableGrid.setSpacing (true);
		tableGrid.addComponent(permTable);
		tableGrid.addComponent (allPerms);
		content.addComponent (buttonGrid);
		content.addComponent (tableGrid);
        this.setContent (content);
	}

}
