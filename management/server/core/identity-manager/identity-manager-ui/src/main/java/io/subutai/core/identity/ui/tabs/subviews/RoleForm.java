package io.subutai.core.identity.ui.tabs.subviews;

import java.util.*;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItem;
import com.vaadin.server.Page;
import com.vaadin.ui.*;
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


		final Button remove = new Button ("Remove");
		remove.setData (newItemId);
		remove.addClickListener (new Button.ClickListener()
		{
			@Override
			public void buttonClick (Button.ClickEvent event)
			{
				for (int i = 0; i < perms.size(); ++i)
				{
					if (perms.get (i).getId() == p.getId())
					{
						perms.remove (i);
						break;
					}
				}
				permTable.removeItem (remove.getData());
			}
		});
		row.getItemProperty ("Remove").setValue (remove);
	}


	private void addPerms (RolesTab callback)
	{
		for (final Permission p : callback.getIdentityManager().getAllPermissions())
		{
			final Object newItemId = allPerms.addItem();
			final Item row = allPerms.getItem (newItemId);

			row.getItemProperty ("Permission").setValue (p.getObjectName());

			Button add = new Button ("Add");
			add.addClickListener (new Button.ClickListener()
			{
				@Override
				public void buttonClick (Button.ClickEvent event)
				{
					boolean exists = false;
					for (Permission permission : perms)
					{
						if (p.getId() == permission.getId())
						{
							exists = true;
							break;
						}
					}
					if (!exists)
					{
						final Permission newPermission = new Permission()
						{
							@Id
							@GeneratedValue(strategy = GenerationType.IDENTITY)
							@Column(name = "id")
							private long id;

							@Column(name = "object")
							private int object;

							@Column(name = "scope")
							private int scope = 1;

							@Column(name = "read")
							private boolean read = false;

							@Column(name = "write")
							private boolean write = false;

							@Column(name = "update")
							private boolean update = false;

							@Column(name = "delete")
							private boolean delete = false;


							@Override
							public Long getId()
							{
								return id;
							}


							@Override
							public void setId(final Long id)
							{
								this.id = id;
							}


							@Override
							public int getObject()
							{
								return object;
							}


							@Override
							public void setObject(int object)
							{
								this.object = object;
							}


							@Override
							public int getScope()
							{
								return scope;
							}


							@Override
							public void setScope(int scope)
							{
								this.scope = scope;
							}


							@Override
							public boolean isRead()
							{
								return read;
							}


							@Override
							public void setRead(boolean read)
							{
								this.read = read;
							}


							@Override
							public boolean isWrite()
							{
								return write;
							}


							@Override
							public void setWrite(boolean write)
							{
								this.write = write;
							}


							@Override
							public boolean isUpdate()
							{
								return update;
							}


							@Override
							public void setUpdate(boolean update)
							{
								this.update = update;
							}


							@Override
							public boolean isDelete()
							{
								return delete;
							}


							@Override
							public void setDelete(boolean delete)
							{
								this.delete = delete;
							}

							@Override
							public String getObjectName()
							{
								return PermissionObject.values()[object - 1].getName();
							}

							@Override
							public List<String> asString()
							{
								List<String> perms = new ArrayList<>();

								if (PermissionObject.values()[object - 1] == PermissionObject.KarafServerAdministration)
								{
									perms.add("admin");
								} else if (PermissionObject.values()[object - 1] == PermissionObject.KarafServerManagement)
								{
									perms.add("manager");
								} else
								{
									String permString = "";

									permString += (PermissionObject.values())[object - 1].getName() + "|A|";
									//permString +="|"+(PermissionScope.values())[scope-1].getName()+"|";

									if (read)
										perms.add(permString + "Read");
									if (write)
										perms.add(permString + "Write");
									if (update)
										perms.add(permString + "Update");
									if (delete)
										perms.add(permString + "Delete");
								}


								return perms;
							}
						};
						newPermission.setId(p.getId());
						newPermission.setScope(1);
						newPermission.setRead(false);
						newPermission.setWrite(false);
						newPermission.setUpdate(false);
						newPermission.setDelete(false);
						// TODO: newPermission.setObject()
						perms.add(newPermission);
						Object newPerm = permTable.addItem();
						Item item = permTable.getItem(newPerm);
						item.getItemProperty("Permission").setValue(row.getItemProperty("Permission").getValue());

						ComboBox scopes = new ComboBox();
						scopes.setNullSelectionAllowed(false);
						scopes.setTextInputAllowed(false);
						for (int i = 1; i < 4; ++i)
						{
							scopes.addItem(i);
							switch (i)
							{
								case (1):
								{
									scopes.setItemCaption(1, "All Objects");
									break;
								}
								case (2):
								{
									scopes.setItemCaption(2, "Child Objects");
									break;
								}
								case (3):
								{
									scopes.setItemCaption(3, "Owner Objects");
									break;
								}
							}
						}
						scopes.setValue(1);
						scopes.addValueChangeListener(new Property.ValueChangeListener()
						{
							@Override
							public void valueChange(Property.ValueChangeEvent event)
							{
								int scope = (int) event.getProperty().getValue();
								for (Permission perm : perms)
								{
									if (newPermission.getId() == perm.getId())
									{
										perm.setScope(scope);
									}
								}
							}
						});
						item.getItemProperty("Scope").setValue(scopes);

						CheckBox read = new CheckBox();
						read.setValue(false);
						read.addValueChangeListener(new Property.ValueChangeListener()
						{
							@Override
							public void valueChange(Property.ValueChangeEvent event)
							{
								boolean v = (boolean) event.getProperty().getValue();
								for (Permission perm : perms)
								{
									if (newPermission.getId() == perm.getId())
									{
										perm.setRead(v);
									}
								}
							}
						});
						item.getItemProperty("Read").setValue(read);

						CheckBox write = new CheckBox();
						write.setValue(false);
						write.addValueChangeListener(new Property.ValueChangeListener()
						{
							@Override
							public void valueChange(Property.ValueChangeEvent event)
							{
								boolean v = (boolean) event.getProperty().getValue();
								for (Permission perm : perms)
								{
									if (newPermission.getId() == perm.getId())
									{
										perm.setWrite(v);
									}
								}
							}
						});
						item.getItemProperty("Write").setValue(write);

						CheckBox update = new CheckBox();
						update.setValue(false);
						update.addValueChangeListener(new Property.ValueChangeListener()
						{
							@Override
							public void valueChange(Property.ValueChangeEvent event)
							{
								boolean v = (boolean) event.getProperty().getValue();
								for (Permission perm : perms)
								{
									if (newPermission.getId() == perm.getId())
									{
										perm.setUpdate(v);
									}
								}
							}
						});
						item.getItemProperty("Update").setValue(update);

						CheckBox delete = new CheckBox();
						delete.setValue(false);
						delete.addValueChangeListener(new Property.ValueChangeListener()
						{
							@Override
							public void valueChange(Property.ValueChangeEvent event)
							{
								boolean v = (boolean) event.getProperty().getValue();
								for (Permission perm : perms)
								{
									if (newPermission.getId() == perm.getId())
									{
										perm.setDelete(v);
									}
								}
							}
						});
						item.getItemProperty("Delete").setValue(delete);


						final Button remove = new Button("Remove");
						remove.setData(newItemId);
						remove.addClickListener(new Button.ClickListener()
						{
							@Override
							public void buttonClick(Button.ClickEvent event)
							{
								for (int i = 0; i < perms.size(); ++i)
								{
									if (perms.get(i).getId() == newPermission.getId())
									{
										perms.remove(i);
									}
								}
								permTable.removeItem(remove.getData());
							}
						});
						item.getItemProperty("Remove").setValue(remove);
					}
					else
					{
						Notification notif = new Notification ("Permission is already added");
						notif.setDelayMsec (2000);
						notif.show (Page.getCurrent());
					}
				}
			});
			row.getItemProperty ("Add").setValue(add);
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

		allPerms.addContainerProperty("Permission", String.class, null);
		allPerms.addContainerProperty ("Add", Button.class, null);

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
